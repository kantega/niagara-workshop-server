package no.kantega.niagara.workshop;

import no.kantega.niagara.broker.ProducerRecord;
import no.kantega.niagara.broker.TopicName;
import org.junit.Test;
import org.kantega.niagara.Source;
import org.kantega.niagara.Sources;

public class Workshopsolver {

    public static final String server   = "localhost";
    public static final String teamNick = "jalla";

    public static final Client.WS ws =
      Client.websocket(server, 8080);


    @Test
    public void first() {

        //Brokeren bruker topic på å rute meldingen rett

        ProducerRecord input =
          ProducerRecord.message(TopicName.solution(teamNick),teamNick);

        Source<ProducerRecord> source =
          Sources.nil();

        ws.run(source);
    }


}
