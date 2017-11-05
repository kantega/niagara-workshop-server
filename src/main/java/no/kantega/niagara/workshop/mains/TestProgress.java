package no.kantega.niagara.workshop.mains;

import no.kantega.niagara.workshop.Client;
import no.kantega.niagara.workshop.Util;
import org.kantega.niagara.Sources;

import java.net.URISyntaxException;

import static no.kantega.niagara.workshop.SubscribeTo.*;

public class TestProgress {

    public static void main(String[] args) throws URISyntaxException {

        Client.websocket("localhost", 8080, replayAndSubscribeTo("/progress/jalla"))
          .run(input ->
            input
              .apply(consumerRecord -> Util.println(consumerRecord.toString()))
              .bind(u -> Sources.nil()));


    }
}
