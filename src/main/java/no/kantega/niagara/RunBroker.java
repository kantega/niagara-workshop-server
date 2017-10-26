package no.kantega.niagara;

import no.kantega.niagara.broker.Broker;
import no.kantega.niagara.work.Util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RunBroker {
    public static void main(String[] args) {

        Broker          broker          = new Broker();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        broker.start(executorService).bind(server ->
          Util.println("Server started").execute()
        );

    }
}
