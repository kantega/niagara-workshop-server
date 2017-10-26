package no.kantega.niagara;

import no.kantega.niagara.broker.Client;
import no.kantega.niagara.broker.SubscribeTo;
import no.kantega.niagara.work.server.WsServerApp;

public class RunWorkshopServer {

    public static void main(String[] args) {
        Client.WS ws =
          Client.websocket("localhost", 8080, SubscribeTo.replayAndSubscribeTo("/firehose"));

        ws.run(new WsServerApp());
    }
}
