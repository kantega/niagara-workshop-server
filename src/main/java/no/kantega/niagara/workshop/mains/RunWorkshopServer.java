package no.kantega.niagara.workshop.mains;

import fj.data.List;
import no.kantega.niagara.workshop.Client;
import no.kantega.niagara.workshop.SubscribeTo;
import no.kantega.niagara.workshop.server.WsServerApp;

import static no.kantega.niagara.workshop.SubscribeTo.*;

public class RunWorkshopServer {

    public static void main(String[] args) {
        Client.WS ws =
          Client.websocket("10.80.8.187", 8080);

        Client.run(ws, List.arrayList(replayAndSubscribeTo("/start"), replayAndSubscribeTo("/solution")), new WsServerApp());
    }
}
