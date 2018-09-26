package no.kantega.niagara.workshop.mains;

import no.kantega.niagara.stream.Sources;
import no.kantega.niagara.workshop.Client;
import no.kantega.niagara.workshop.Util;

import java.net.URISyntaxException;

public class TestProgress {

    public static void main(String[] args) throws URISyntaxException, InterruptedException {

        Client.run(Client.websocket(Settings.brokerIp, Settings.brukerPort)
          ,"/progress/jalla",input ->
            input
              .apply(consumerRecord -> Util.println(consumerRecord.toString()))
              .bind(u -> Sources.nil()));
        Thread.sleep(1000);


    }
}
