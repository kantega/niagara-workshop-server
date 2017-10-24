package no.kantega.niagara.test;

import no.kantega.niagara.broker.Client;
import org.junit.Test;
import org.kantega.niagara.Sources;

import java.time.Duration;
import java.util.Arrays;

public class TestSolver {

    public static Client.WS ws =
      Client.websocket(8080, "localhost", "/solution/balla");

    @Test
    public void task1() {
        Client.post(8080, "localhost", "/solution/balla", "att")
          .executeAndAwait(Duration.ofSeconds(1));
    }

    @Test
    public void task2() {
        Client.post(8080, "localhost", "/solution/balla", "att")
          .executeAndAwait(Duration.ofSeconds(1));
    }


    @Test
    public void task3() {
        Client.post(8080, "localhost", "/solution/balla", "atle")
          .executeAndAwait(Duration.ofSeconds(1));
    }


    @Test
    public void task4() {
        ws.run(
          Sources.value("atle")
        );
    }

    @Test
    public void task5() {
        ws.
          run(
            Sources.value("atle")
              .map(String::toUpperCase)
          );
    }

    @Test
    public void task6() {
        ws.run(
          Sources.value("atle")
            .map(String::toUpperCase)
            .flatten(n -> Arrays.asList(n.split("")))
        );
    }

    @Test
    public void task7() {
        ws.run(
          Sources.value("atle")
            .append(() -> Sources.value("atle"))
            .flatten(n -> Arrays.asList(n.split("")))
        );
    }


}
