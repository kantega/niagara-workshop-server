package no.kantega.niagara.workshop.mains;

import no.kantega.niagara.broker.ConsumerRecord;
import no.kantega.niagara.broker.ProducerRecord;
import no.kantega.niagara.workshop.Client;
import no.kantega.niagara.workshop.SubscribeTo;
import no.kantega.niagara.workshop.Util;
import org.junit.Test;
import org.kantega.niagara.Sources;
import org.kantega.niagara.Stream;

import java.time.Duration;
import java.util.Arrays;

import static no.kantega.niagara.broker.ProducerRecord.message;
import static no.kantega.niagara.broker.ProducerRecord.toMessage;
import static no.kantega.niagara.broker.TopicName.solution;
import static no.kantega.niagara.broker.TopicName.start;

public class TestSolver {

    public static Client.WS ws =
      Client.websocket("localhost", 8080);

    @Test
    public void task3() {
        ws.run(
          Sources.emitOne(message(start("jalla"), "start"))
        );
    }

    @Test
    public void task4() {
        ws.run(
          Sources.emitOne(message(solution("jalla"), "atle"))
        );
    }

    @Test
    public void task5() {
        ws.
          run(
            Sources.emitOne("atle")
              .map(String::toUpperCase)
              .map(toMessage(solution("jalla")))
          );
    }

    @Test
    public void task6() {
        ws.run(
          Sources.emitOne("atle")
            .map(String::toUpperCase)
            .flatten(n -> Arrays.asList(n.split("")))
            .map(toMessage(solution("jalla")))
        );
    }

    @Test
    public void task7() {
        ws.run(
          Sources.emitOne("atle")
            .append(() -> Sources.emitOne("atle"))
            .flatten(n -> Arrays.asList(n.split("")))
            .map(toMessage(solution("jalla")))
        );
    }

    public static Client.WS ws2 =
      Client.websocket("localhost", 8080, SubscribeTo.replayAndSubscribeTo("/echo"));


    @Test
    public void task8fail() {

    }

    @Test
    public void task8() throws InterruptedException {
        Stream<ConsumerRecord,ProducerRecord> app =
          incoming->incoming.map(cr-> new ProducerRecord(solution("jalla"),cr.msg));
        ws2.run(
          app
        ).await(Duration.ofSeconds(5));
    }

    @Test
    public void task9() throws InterruptedException {
        Stream<ConsumerRecord,ProducerRecord> app =
          incoming->incoming.foldLeft("",(cum,in)->cum + in.msg).map(m-> new ProducerRecord(solution("jalla"),m));
        ws2.run(
          app
        ).await(Duration.ofSeconds(5));
    }

}
