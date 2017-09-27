package no.kantega.niagara.work.server;

import no.kantega.niagara.work.Client;

public class TestClient {


    public static void main(String[] args) {


        Client
          .openWebsocket(8080,"localhost","tasks/a")
          .execute().onComplete(att->att.doEffect(Throwable::printStackTrace, ws->ws.handler(System.out::println)));

    }



}
