package no.kantega.niagara.workshop.mains;

import no.kantega.niagara.broker.Broker;
import no.kantega.niagara.workshop.Util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RunBroker {
    public static void main(String[] args) {

        Broker          broker          = new Broker();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        broker.start(executorService).bind(server ->
          Util.println("Server started").execute()
        );

    }
}
