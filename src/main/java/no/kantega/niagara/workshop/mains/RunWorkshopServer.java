package no.kantega.niagara.workshop.mains;

import no.kantega.niagara.workshop.Client;
import no.kantega.niagara.workshop.SubscribeTo;
import no.kantega.niagara.workshop.server.WsServerApp;

import static no.kantega.niagara.workshop.SubscribeTo.*;

public class RunWorkshopServer {

    public static void main(String[] args) {
        Client.WS ws =
          Client.websocket("172.16.0.168", 8080, replayAndSubscribeTo("/start"), replayAndSubscribeTo("/solution"));

        ws.run(new WsServerApp());
    }
}
