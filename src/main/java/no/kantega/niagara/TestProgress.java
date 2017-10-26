package no.kantega.niagara;

import no.kantega.niagara.broker.Client;
import no.kantega.niagara.work.Util;
import org.kantega.niagara.Sources;

import java.net.URISyntaxException;

import static no.kantega.niagara.broker.SubscribeTo.*;

public class TestProgress {

    public static void main(String[] args) throws URISyntaxException {

          Client.websocket("localhost", 8080, replayAndSubscribeTo("/progress/jalla"))
            .open(input->
              input.apply(consumerRecord -> Util.println(consumerRecord.toString())).bind(u-> Sources.nil())).execute();


    }
}
