package no.kantega.niagara.server;

import fj.F;
import fj.data.Either;
import fj.data.Option;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.kantega.niagara.Source;
import org.kantega.niagara.Task;
import org.kantega.niagara.exchange.Topic;

import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public void start(F<Source<Either<RoutingContext, IncomingWsMessage>>, Source<OutgoingWsMessage>> app) throws URISyntaxException {

        Topic<Either<RoutingContext, IncomingWsMessage>> incoming = new Topic<>();

        ConcurrentHashMap<String, ServerWebSocket> openSockets = new ConcurrentHashMap<>();

        Vertx vertx = Vertx.vertx();

        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.route().handler(routingContext -> {
            System.out.println("Request at " + routingContext.normalisedPath());
            incoming.publish(Either.left(routingContext)).execute();
            routingContext.next();
        });

        server
          .websocketHandler(serverWebSocket -> {
              System.out.println("Websocket connection established " + serverWebSocket.path());
              String uuid = UUID.randomUUID().toString();
              openSockets.put(uuid, serverWebSocket);
              serverWebSocket.handler(buffer -> incoming.publish(Either.right(new IncomingWsMessage(serverWebSocket, uuid, buffer.toString()))));
              serverWebSocket.closeHandler(v -> openSockets.remove(uuid));
          })
          .requestHandler(router::accept)
          .listen(8080);

        app.f(incoming.subscribe()).apply(msg -> Task.runnableTask(() ->
          Option.fromNull(openSockets.get(msg.connectionId)).foreachDoEffect(ws -> ws.write(Buffer.buffer(msg.message)))
        ));
    }


}