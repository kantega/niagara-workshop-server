package no.kantega.niagara.workshop;

import fj.Unit;
import fj.data.List;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import no.kantega.niagara.broker.ConsumerRecord;
import no.kantega.niagara.broker.ProducerRecord;
import no.kantega.niagara.broker.TopicName;
import org.kantega.kson.codec.JsonCodec;
import org.kantega.kson.codec.JsonCodecs;
import org.kantega.niagara.*;
import org.kantega.niagara.exchange.Topic;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static fj.P.p;
import static fj.Unit.*;
import static no.kantega.niagara.workshop.Util.*;
import static org.kantega.kson.parser.JsonParser.parse;
import static org.kantega.kson.parser.JsonWriter.*;
import static org.kantega.niagara.Task.*;

public class Client {

    static private Vertx vertx = Vertx.vertx();

    static final HttpClient httpClient =
      vertx.createHttpClient();

    static ScheduledExecutorService ses =
      Executors.newSingleThreadScheduledExecutor();

    public static WS websocket(String server, int port, SubscribeTo... subscribeTos) {

        String path =
          "/ws" + toQueryString(true, List.arrayList(subscribeTos));

        return app -> async(cb -> {
            Topic<ConsumerRecord>          topic   = new Topic<>();
            CompletableFuture<Source.Stop> stopper = new CompletableFuture<>();


            httpClient.websocket(port, server, path, ws -> {
                ws.handler(buffer ->
                  parse(buffer.toString())
                    .decode(consumerRecordCodec.decoder)
                    .fold(
                      err -> fail(new RuntimeException(err)).toUnit(),
                      topic::publish)
                    .onFail(t -> println(t.getMessage()))
                    .execute());


                ws.closeHandler(u ->
                  stopper.complete(Source.stop));


                println("Opened connection to " + server + ":" + port + path)
                  .andThen(
                    app
                      .apply(topic.subscribe())
                      .apply(out->Util.println("Sending "+out.topic.name+":"+out.msg).thenJust(out))
                      .apply(out -> runnableTask(() -> ws.writeTextMessage(write(producerRecordCodec.encode(out)))))
                      .closeOn(Eventually.wrap(stopper))
                      .onClose(close(ws))
                      .toTask())
                  .bind(closed -> println("Closed connection to " + server + ":" + port + path))
                  .bind(u -> runnableTask(() -> cb.f(Attempt.value(unit()))))
                  .execute();
            }, t ->
              cb.f(Attempt.fail(t)));

        });
    }


    public static Task<Unit> close(WebSocket ws) {
        return runnableTask(() -> {
            try {
                ws.close();
            } catch (Exception e) {
                System.out.println("Tried to close already closed websocket");
            }
        }).andThen(println("Closed " + ws));
    }

    public static Eventually<Unit> run(WS ws, Stream<ConsumerRecord, ProducerRecord> app) {
       return ws.open(app)
          .andThen(Util.println("Client closed, trying to reconnect"))
          .andThen(Task.runnableTask(() ->
            run(ws, app)
          ).delay(Duration.ofSeconds(5), ses))
          .execute();
    }

    public static void run(WS ws, Source<ProducerRecord> output) {
        ws.open(input -> output).execute().await(Duration.ofSeconds(10));
    }

    public interface WS {

        Task<Unit> open(Stream<ConsumerRecord, ProducerRecord> app);

        default Eventually<Unit> run(Stream<ConsumerRecord, ProducerRecord> app) {
            return Client.run(this, app);
        }

        default void run(Source<ProducerRecord> output) {
            Client.run(this, output);
        }
    }

    public static final JsonCodec<ProducerRecord> producerRecordCodec =
      JsonCodecs.objectCodec(
        JsonCodecs.field("topic", JsonCodecs.stringCodec.xmap(topicName -> topicName.name, TopicName::new)),
        JsonCodecs.field("msg", JsonCodecs.stringCodec),
        record -> p(record.topic, record.msg),
        ProducerRecord::new
      );

    public static final JsonCodec<ConsumerRecord> consumerRecordCodec =
      JsonCodecs.objectCodec(
        JsonCodecs.field("id", JsonCodecs.stringCodec),
        JsonCodecs.field("offset", JsonCodecs.longCodec),
        JsonCodecs.field("topic", JsonCodecs.stringCodec.xmap(topicName -> topicName.name, TopicName::new)),
        JsonCodecs.field("msg", JsonCodecs.stringCodec),
        record -> p(record.id, record.offset, record.topic, record.msg),
        ConsumerRecord::new
      );

    private static String toQueryString(boolean first, List<SubscribeTo> subscribeToList) {
        return
          subscribeToList.isEmpty() ? "" :
            (first ? "?" : "&") +
              subscribeToList.head().topic + "=" +
              (subscribeToList.head().replay ? "first" : "last") +
              toQueryString(false, subscribeToList.tail());
    }
}
