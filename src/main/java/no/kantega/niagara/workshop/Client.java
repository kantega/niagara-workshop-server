package no.kantega.niagara.workshop;

import fj.Unit;
import fj.data.List;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import no.kantega.niagara.broker.ConsumerRecord;
import no.kantega.niagara.broker.ProducerRecord;
import no.kantega.niagara.broker.TopicName;
import no.kantega.niagara.exchange.Topic;
import no.kantega.niagara.stream.*;
import org.kantega.kson.codec.JsonCodec;
import org.kantega.kson.codec.JsonCodecs;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static fj.P.p;
import static fj.Unit.*;
import static no.kantega.niagara.broker.ProducerRecord.*;
import static no.kantega.niagara.broker.TopicName.*;
import static no.kantega.niagara.stream.Task.*;
import static no.kantega.niagara.workshop.Util.*;
import static org.kantega.kson.parser.JsonParser.parse;
import static org.kantega.kson.parser.JsonWriter.*;

public class Client {

    static private Vertx vertx = Vertx.vertx();

    static final HttpClient httpClient =
      vertx.createHttpClient();

    static ScheduledExecutorService ses =
      Executors.newSingleThreadScheduledExecutor();

    public static WS websocket(String hostname, int port) {


        return (subscribes, app) -> async(cb -> {
            String path =
              "/ws" + toQueryString(true, subscribes);
            Topic<ConsumerRecord>          topic   = new Topic<>();
            CompletableFuture<Source.Stop> stopper = new CompletableFuture<>();


            httpClient.websocket(port, hostname, path, ws -> {
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


                println("Opened connection to " + hostname + ":" + port + path)
                  .andThen(
                    app
                      .apply(topic.subscribe())
                      .apply(out -> Util.println("Sending " + out.topic.name + ":" + out.msg).thenJust(out))
                      .apply(out -> runnableTask(() -> ws.writeTextMessage(write(producerRecordCodec.encode(out)))))
                      .closeOn(Eventually.wrap(stopper))
                      .onClose(close(ws))
                      .toTask())
                  .bind(closed -> println("Closed connection to " + hostname + ":" + port + path))
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

    public static void run(WS ws, String id, String subscribe, Stream<String, String> app) {
        Stream<ConsumerRecord, ProducerRecord> wrapped =
          incoming -> incoming.map(rec -> rec.msg).through(app).map(toMessage(solution(id)));
        run(ws, subscribe, wrapped);
    }

    public static void run(WS ws, String subscribe, Stream<ConsumerRecord, ProducerRecord> app) {
        run(ws, List.single(SubscribeTo.replayAndSubscribeTo(subscribe)), app);
    }

    public static void run(WS ws,  Stream<ConsumerRecord, ProducerRecord> app) {
        run(ws, List.nil(), app);
    }

    public static void run(WS ws, List<SubscribeTo> subscribes, Stream<ConsumerRecord, ProducerRecord> app) {
        ws
          .open(subscribes, app)
          .andThen(Util.println("Client closed, trying to reconnect"))
          .andThen(runnableTask(() ->
            run(ws, subscribes, app)
          ).delay(Duration.ofSeconds(5), ses))
          .execute().await(Duration.ofSeconds(5));
    }

    public static void run(WS ws, String id, Source<String> output) {
        run(ws, output.map(toMessage(solution(id))));
    }

    public static void run(WS ws, Source<ProducerRecord> output) {
        ws.open(List.nil(), input -> output).execute().await(Duration.ofSeconds(10));
    }

    public interface WS {

        Task<Unit> open(List<SubscribeTo> subscribeToList, Stream<ConsumerRecord, ProducerRecord> app);

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
