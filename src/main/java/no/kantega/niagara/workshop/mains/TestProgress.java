package no.kantega.niagara.workshop.mains;

import no.kantega.niagara.stream.Sources;
import no.kantega.niagara.workshop.Client;
import no.kantega.niagara.workshop.Util;

import java.net.URISyntaxException;

public class TestProgress {

    public static void main(String[] args) throws URISyntaxException {

        Client.run(Client.websocket("10.80.8.187", 8080)
          ,"/progress/jalla",input ->
            input
              .apply(consumerRecord -> Util.println(consumerRecord.toString()))
              .bind(u -> Sources.nil()));


    }
}
