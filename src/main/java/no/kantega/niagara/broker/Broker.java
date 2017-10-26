package no.kantega.niagara.broker;

import fj.data.List;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import no.kantega.niagara.work.Util;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.kantega.kson.JsonResult;
import org.kantega.niagara.Eventually;
import org.kantega.niagara.Source;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static fj.Equal.stringEqual;
import static fj.data.List.*;
import static no.kantega.niagara.broker.Client.producerRecordCodec;
import static no.kantega.niagara.broker.ClientSubscription.firehose;
import static no.kantega.niagara.broker.ClientSubscription.subscription;
import static org.kantega.kson.parser.JsonParser.parse;

public class Broker {

    public Eventually<HttpServer> start(ExecutorService executor) {
        return Eventually.callback(cb -> {
            executor.execute(() -> {

                PersistentQueue queue =
                  PersistentQueue.log(executor);

                Vertx vertx =
                  Vertx.vertx();

                HttpServer server =
                  vertx.createHttpServer();

                Router router =
                  Router.router(vertx);

                router.route().handler(BodyHandler.create());

                router.route().handler(routingContext -> {
                    routingContext.response().setStatusCode(404).end();
                });

                server
                  .websocketHandler(serverWebSocket -> {

                      System.out.println("Connection path: "+serverWebSocket.path()+", query: "+serverWebSocket.query());

                      String path =
                        serverWebSocket.path() == null ? "":serverWebSocket.path();

                      String query =
                        serverWebSocket.query() == null ? "":serverWebSocket.query();

                      List<NameValuePair> nameValuePairs =
                        iterableList(URLEncodedUtils.parse(query, Charset.forName("UTF-8")));

                      boolean firehose =
                        nameValuePairs
                          .exists(np -> stringEqual.eq(np.getValue(), "firehose"));

                      boolean replay =
                        nameValuePairs
                          .exists(np -> stringEqual.eq(np.getName(), "replay") && stringEqual.eq(np.getValue(), "true"));

                      List<String> topics =
                        nameValuePairs
                          .filter(np -> stringEqual.eq(np.getName(), "topic"))
                          .map(NameValuePair::getValue);


                      if (path.startsWith("/ws")) {

                          ClientSubscription sub =
                            firehose ?
                              firehose(serverWebSocket) :
                              subscription(serverWebSocket, topics);

                          CompletableFuture<Source.Stop> closer =
                            new CompletableFuture<>();

                          (replay ? queue.subscribe(0) : queue.subscribeToLast())
                            .to(sub)
                            .closeOn(Eventually.wrap(closer))
                            .onClose(Util.println("Websocket closed "+serverWebSocket.query()))
                            .toTask()
                            .using(executor)
                            .execute();


                          serverWebSocket.handler(buffer -> {
                              JsonResult<ProducerRecord> jr =
                                parse(buffer.toString()).decode(producerRecordCodec.decoder);

                              queue.consume(jr.orThrow()).execute();
                          });


                          serverWebSocket.closeHandler(v -> {
                              closer.complete(Source.stop);

                          });
                      } else
                          serverWebSocket.reject();


                  })
                  .requestHandler(router::accept)
                  .listen(8080);
            });

        });

    }
}

