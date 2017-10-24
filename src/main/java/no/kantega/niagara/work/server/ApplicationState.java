package no.kantega.niagara.work.server;

import fj.P2;
import fj.data.List;
import no.kantega.niagara.broker.ConnectionId;
import no.kantega.niagara.server.InMessage;
import no.kantega.niagara.server.OutMessage;
import no.kantega.niagara.server.Route;
import no.kantega.niagara.broker.PersistentQueue;
import org.kantega.kson.codec.JsonEncoder;
import org.kantega.kson.json.JsonValues;
import org.kantega.kson.parser.JsonWriter;
import org.kantega.niagara.Mealy;
import org.kantega.niagara.Task;

import static fj.P.p;
import static no.kantega.niagara.server.Matcher.*;
import static no.kantega.niagara.server.OutMessage.*;
import static no.kantega.niagara.work.server.SessionId.sessId;
import static org.kantega.kson.codec.JsonDecoders.obj;
import static org.kantega.kson.json.JsonValues.jObj;
import static org.kantega.kson.json.JsonValues.jString;

public class ApplicationState implements Mealy<InMessage, Task<List<OutMessage>>> {

    //Used to post data and get data to solve the quest
    private static final Route wsSolutionRoute =
      Route.route("/solution/:id");

    //Can be used to post answers to the quest
    private static final Route requestSolutionRoute =
      Route.route("/solution/:id");

    //Used by webclient to display tasks and progress
    private static final Route progressRoute =
      Route.route("/progress/:id");


    private final List<P2<SessionId, ConnectionId>> progressConnections;
    private final List<P2<SessionId, ConnectionId>> solutionConnections;
    private final List<ConnectionId>                statusConnections;
    private final PersistentQueue<String>           queue;
    private final WorkshopState                     workshopState;


    public ApplicationState(
      List<P2<SessionId, ConnectionId>> progressConnections,
      List<P2<SessionId, ConnectionId>> solutionConnections,
      List<ConnectionId> statusConnections,
      PersistentQueue<String> queue,
      WorkshopState workshopState) {
        this.progressConnections = progressConnections;
        this.solutionConnections = solutionConnections;
        this.statusConnections = statusConnections;
        this.queue = queue;
        this.workshopState = workshopState;
    }


    public static ApplicationState newEmpty(PersistentQueue<String> queue, WorkshopState initWsState) {
        return new ApplicationState(List.nil(), List.nil(), List.nil(), queue, initWsState);
    }

    public Transition<InMessage, Task<List<OutMessage>>> apply(
      InMessage msg) {

        P2<ApplicationState, Task<List<OutMessage>>> cmd =
          wsOpen(progressRoute, (m, params) ->
            params.get("id").map(
              id ->
                addProgressMapping(sessId(id), m.connectionId)
                  .progress(new ProgressInput(sessId(id), "start"))
                  .map2(task ->
                    task.flatMap(outs -> queue.messages("out-" + id).map(ks -> ks.map(k -> webSocketReply(m.connectionId, k.value))).map(fromQueue -> fromQueue.append(outs)))
                  )))
            .or(
              wsClose(progressRoute, (m, params) ->
                params.get("id").map(
                  id -> p(removeProgressMapping(m.connectionId), Task.value(List.nil()))
                )))
            .or(
              request(requestSolutionRoute, (m, params) ->
                params.get("id").map(
                  id -> progress(new ProgressInput(sessId(id), m.body))
                )))
            .or(
              wsOpen(wsSolutionRoute, (m, params) ->
                params.get("id").map(
                  id -> p(addSolutionMapping(sessId(id), m.connectionId), Task.value(List.nil()))
                )))
            .or(
              wsClose(requestSolutionRoute, (m, params) ->
                params.get("id").map(
                  id -> p(removeSolutionMapping(m.connectionId), Task.value(List.nil()))
                )))
            .or(
              wsText(requestSolutionRoute, (m, params) ->
                params.get("id").map(
                  id -> progress(new ProgressInput(sessId(id), m.text))
                )))

            //Utfør match
            .matches(msg)
            .bind(i -> i)
            //ellers ingenting
            .orSome(p(this, Task.value(List.nil())));

        return Mealy.transition(cmd._1(), cmd._2());
    }

    public ApplicationState addProgressMapping(SessionId sessionId, ConnectionId connectionId) {
        return new ApplicationState(progressConnections.cons(p(sessionId, connectionId)), solutionConnections, statusConnections, queue, workshopState);
    }

    public ApplicationState removeProgressMapping(ConnectionId connectionId) {
        return new ApplicationState(progressConnections.filter(mapping -> !mapping._2().equals(connectionId)), solutionConnections, statusConnections, queue, workshopState);
    }

    public ApplicationState addSolutionMapping(SessionId sessionId, ConnectionId connectionId) {
        return new ApplicationState(progressConnections, solutionConnections.cons(p(sessionId, connectionId)), statusConnections, queue, workshopState);
    }

    public ApplicationState removeSolutionMapping(ConnectionId connectionId) {
        return new ApplicationState(progressConnections, solutionConnections.filter(mapping -> !mapping._2().equals(connectionId)), statusConnections, queue, workshopState);
    }

    public ApplicationState withNewProgress(WorkshopState workshopState) {
        return new ApplicationState(progressConnections, solutionConnections, statusConnections, queue, workshopState);
    }

    public P2<ApplicationState, Task<List<OutMessage>>> progress(ProgressInput input) {

        P2<WorkshopState, List<WorkshopStateMsg>> next =
          workshopState.handle(input);

        String msgAsString =
          JsonWriter.write(msgEncoder.encode(input));

        return
          p(
            withNewProgress(next._1()),
            queue.append("input", msgAsString)
              .andJust(distributeToProgress(next._2()).append(distributeToStatus(next._2())))
          );
    }

    public List<OutMessage> distributeToProgress(
      List<WorkshopStateMsg> msgs) {
        return
          msgs.reverse()
            .bind(msg ->
              progressConnections
                .filter(mapping -> SessionId.eq.eq(mapping._1(), msg.sessionId))
                .map(mapping -> new WebsocketReply(mapping._2(), msg.progressMsg.message)));
    }


    public List<OutMessage> distributeToStatus(
      List<WorkshopStateMsg> msgs) {
        return
          msgs.reverse()
            .bind(msg ->
              statusConnections
                .map(mapping -> new WebsocketReply(mapping, msg.progressMsg.message)));
    }





    static JsonEncoder<ProgressInput> msgEncoder =
      req -> jObj(
        JsonValues.field("message", jString(req.message)),
        JsonValues.field("sessionId", jString(req.sessionId.value))
      );
}