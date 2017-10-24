package no.kantega.niagara.test;

import no.kantega.niagara.broker.Client;
import no.kantega.niagara.work.Util;
import org.kantega.niagara.Sources;

import java.net.URISyntaxException;

public class TestProgress {

    public static void main(String[] args) throws URISyntaxException {

          Client.websocket(8080, "localhost", "/progress/balla")
            .open(input->
              input.apply(Util::println).bind(u-> Sources.nil())).execute();


    }
}
