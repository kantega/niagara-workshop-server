package no.kantega.niagara.work.server;

import no.kantega.niagara.server.Server;

import java.net.URISyntaxException;
import java.time.Duration;

public class WsServerMain {


    public static void main(String[] args) throws URISyntaxException {

        new Server()
          .start(WsServerApp::run)
          .onComplete(closedAttempt -> closedAttempt.doEffect(
            System.out::println,
            System.out::println
          ));


    }
}
