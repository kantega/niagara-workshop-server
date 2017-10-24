package no.kantega.niagara.broker;

import fj.Unit;
import fj.data.List;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import no.kantega.niagara.work.Util;
import org.kantega.kson.codec.JsonCodec;
import org.kantega.kson.codec.JsonCodecs;
import org.kantega.kson.parser.JsonWriter;
import org.kantega.niagara.*;
import org.kantega.niagara.exchange.Topic;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static fj.P.p;
import static fj.Unit.*;
import static io.vertx.core.buffer.Buffer.*;
import static no.kantega.niagara.work.Util.*;
import static org.kantega.kson.parser.JsonParser.parse;
import static org.kantega.kson.parser.JsonWriter.*;
import static org.kantega.niagara.Task.*;

public class Client {

    static final HttpClient httpClient =
      Vertx.vertx().createHttpClient();


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
                      .apply(out -> runnableTask(() -> ws.write(buffer(write(producerRecordCodec.encode(out))))))
                      .closeOn(Eventually.wrap(stopper))
                      .onClose(close(ws))
                      .toTask())
                  .flatMap(closed -> println(closed.toString()))
                  .flatMap(u -> runnableTask(() -> cb.f(Attempt.value(unit()))))
                  .execute();
            });
        });
    }


    public static Task<Unit> close(WebSocket ws) {
        return runnableTask(ws::close).andThen(println("Closed " + ws));
    }

    public static void run(WS ws, Stream<ConsumerRecord, ProducerRecord> app) {
        ws.open(app).execute().await(Duration.ofSeconds(10));
    }

    public static void run(WS ws, Source<ProducerRecord> output) {
        ws.open(input -> output).execute().await(Duration.ofSeconds(10));
    }

    public interface WS {

        Task<Unit> open(Stream<ConsumerRecord, ProducerRecord> app);

        default void run(Stream<ConsumerRecord, ProducerRecord> app) {
            Client.run(this, app);
        }

        default void run(Source<ProducerRecord> output) {
            Client.run(this, output);
        }

    }

    public static final JsonCodec<ProducerRecord> producerRecordCodec =
      JsonCodecs.objectCodec(
        JsonCodecs.field("topic", JsonCodecs.stringCodec),
        JsonCodecs.field("msg", JsonCodecs.stringCodec),
        record -> p(record.topic, record.msg),
        ProducerRecord::new
      );

    public static final JsonCodec<ConsumerRecord> consumerRecordCodec =
      JsonCodecs.objectCodec(
        JsonCodecs.field("id", JsonCodecs.stringCodec),
        JsonCodecs.field("offset", JsonCodecs.longCodec),
        JsonCodecs.field("topic", JsonCodecs.stringCodec),
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
