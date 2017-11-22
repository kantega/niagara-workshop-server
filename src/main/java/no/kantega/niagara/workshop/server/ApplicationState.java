package no.kantega.niagara.workshop.server;

import fj.P2;
import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;
import no.kantega.niagara.broker.ConsumerRecord;
import no.kantega.niagara.broker.ProducerRecord;
import no.kantega.niagara.broker.TopicName;
import org.kantega.niagara.Mealy;

import static no.kantega.niagara.workshop.server.SessionId.sessionIdOrd;

public class ApplicationState implements Mealy<ConsumerRecord, List<ProducerRecord>> {

    //Used to post data and get data to solve the quest
    private static final Route wsSolutionRoute =
      Route.route("/solution/:id");

    //Used by webclient to display tasks and progress
    private static final Route startRoute =
      Route.route("/start/:id");

    private final TreeMap<SessionId, Team> teams;


    public ApplicationState(TreeMap<SessionId, Team> progressConnections) {
        this.teams = progressConnections;
    }

    public static ApplicationState newEmpty() {
        return new ApplicationState(TreeMap.empty(sessionIdOrd));
    }

    @Override
    public Transition<ConsumerRecord, List<ProducerRecord>> apply(ConsumerRecord producerRecord) {
        TopicName topicName = producerRecord.topic;

        return
          startRoute
            .onMatchParam(topicName.name, (id) -> {
                SessionId sessionId =
                  new SessionId(id);

                P2<Team, List<ProgressOutput>> firstProgress =
                  Team.newTeam(sessionId, producerRecord.msg);

                List<ProducerRecord> output =
                  firstProgress._2().map(msg -> new ProducerRecord(TopicName.progressTopic(sessionId), msg.message));

                return Mealy.transition(addTeam(firstProgress._1()), output);
            })
            .orElse(
              () -> wsSolutionRoute.onMatchParam(topicName.name, (id) -> {
                  SessionId sessionId =
                    new SessionId(id);

                  Option<Team> maybeTeam =
                    teams.get(sessionId);

                  return maybeTeam.map(team -> {

                      P2<Team, List<ProgressOutput>> next =
                        team.apply(new ProgressInput(producerRecord.msg));

                      List<ProducerRecord> output =
                        next._2()
                          .map(msg -> new ProducerRecord(TopicName.progressTopic(sessionId), msg.message))
                          .cons(new ProducerRecord(TopicName.progressTopic(sessionId), "team:" + producerRecord.msg));

                      return Mealy.transition(addTeam(next._1()), output);

                  }).orSome(Mealy.transition(this, List.nil()));
              })
            )
            .orSome(Mealy.transition(this, List.nil()));
    }

    public ApplicationState addTeam(Team team) {
        return new ApplicationState(teams.set(team.sessionId, team));
    }
}