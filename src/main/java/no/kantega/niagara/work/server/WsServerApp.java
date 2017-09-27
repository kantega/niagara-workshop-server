package no.kantega.niagara.work.server;

import fj.data.Either;
import fj.data.List;
import io.vertx.ext.web.RoutingContext;
import no.kantega.niagara.server.IncomingWsMessage;
import no.kantega.niagara.server.OutgoingWsMessage;
import org.kantega.niagara.Source;

public class WsServerApp {

    public Source<OutgoingWsMessage> run(Source<Either<RoutingContext,IncomingWsMessage>> incoming){
        return incoming.flatten(either->{
            return either.either(
              this::handle,
              this::handle
            );
        });

    };


    private Iterable<OutgoingWsMessage> handle(RoutingContext incomingWsMessage){


        return List.nil();
    }

    private Iterable<OutgoingWsMessage> handle(IncomingWsMessage incomingWsMessage){

        return List.nil();
    }

}
