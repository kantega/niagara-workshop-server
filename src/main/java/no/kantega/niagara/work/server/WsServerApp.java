package no.kantega.niagara.work.server;

import no.kantega.niagara.server.InMessage;
import no.kantega.niagara.server.OutMessage;
import no.kantega.niagara.broker.PersistentQueue;
import org.kantega.kson.codec.JsonDecoder;
import org.kantega.kson.parser.JsonParser;
import org.kantega.niagara.Source;
import org.kantega.niagara.Sources;

import java.time.Duration;

import static no.kantega.niagara.work.Util.println;
import static org.kantega.kson.codec.JsonDecoders.field;
import static org.kantega.kson.codec.JsonDecoders.*;
import static org.kantega.kson.json.JsonValues.field;

public class WsServerApp {

    public static Source<OutMessage> run(Source<InMessage> incoming) {
        PersistentQueue<String> q =
          PersistentQueue.log(
            msg -> msg,
            str -> str);

        Source<WorkshopState> init =
          q.replay("incoming")
            .foldLeft(
              WorkshopState.newWorkshop(),
              (state, msg) ->
                state.handle(JsonParser.parse(msg.value).decode(msgDecoder).orThrow())._1()
            );

        WorkshopState inited =
          Sources.last(init).executeAndAwait(Duration.ofSeconds(10)).orThrow().orSome(WorkshopState.newWorkshop());

        return
          incoming
            .apply(inc -> println("Incoming:" + inc.toString()).map(u -> inc))
            .mapWithMealy(ApplicationState.newEmpty(q, inited))
            .apply(t -> t)
            .flatten(i -> i)
            .apply(out -> println("Outgoing:" + out.toString()).map(u -> out))
            .onClose(q.close());
    }




}
