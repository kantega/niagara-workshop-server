package no.kantega.niagara.test;

import no.kantega.niagara.broker.Client;
import org.junit.Test;
import org.kantega.niagara.Sources;

import java.time.Duration;
import java.util.Arrays;

import static no.kantega.niagara.broker.ProducerRecord.message;
import static no.kantega.niagara.broker.ProducerRecord.toMessage;
import static no.kantega.niagara.broker.TopicName.solution;

public class TestSolver {

    public static Client.WS ws =
      Client.websocket("localhost", 8080);



    @Test
    public void task4() {
        ws.run(
          Sources.value(message(solution("/solution"), "atle"))
        );
    }

    @Test
    public void task5() {
        ws.
          run(
            Sources.value("atle")
              .map(String::toUpperCase)
              .map(toMessage(solution("/solution")))
          );
    }

    @Test
    public void task6() {
        ws.run(
          Sources.value("atle")
            .map(String::toUpperCase)
            .flatten(n -> Arrays.asList(n.split("")))
            .map(toMessage(solution("/solution")))
        );
    }

    @Test
    public void task7() {
        ws.run(
          Sources.value("atle")
            .append(() -> Sources.value("atle"))
            .flatten(n -> Arrays.asList(n.split("")))
            .map(toMessage(solution("/solution")))
        );
    }


}
