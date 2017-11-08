package no.kantega.niagara.workshop.mains;

import fj.Ord;
import fj.P;
import fj.data.List;
import fj.data.Option;
import fj.data.Set;
import fj.data.TreeMap;
import no.kantega.niagara.broker.ConsumerRecord;
import no.kantega.niagara.broker.ProducerRecord;
import no.kantega.niagara.broker.TopicName;
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
      Client.websocket("172.16.0.168", 8080);

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
        Stream<ConsumerRecord, ProducerRecord> app =
          incoming -> incoming.map(cr -> new ProducerRecord(solution("jalla"), cr.msg));
        ws2.run(
          app
        ).await(Duration.ofSeconds(5));
    }

    @Test
    public void task9() throws InterruptedException {
        Stream<ConsumerRecord, ProducerRecord> app =
          incoming -> incoming.foldLeft("", (cum, in) -> cum + in.msg).map(m -> new ProducerRecord(solution("jalla"), m));
        ws2.run(
          app
        ).await(Duration.ofSeconds(5));
    }

    public static Client.WS ws3 =
      Client.websocket("localhost", 8080, SubscribeTo.replayAndSubscribeTo("/memberships"));


    @Test
    public void task10() throws InterruptedException {
        Stream<ConsumerRecord, ProducerRecord> app =
          incoming ->
            incoming
              .map(r -> r.msg)
              .mapWithState(TreeMap.<String, Set<String>>empty(Ord.stringOrd), (members, msg) -> {
                  if (msg.startsWith("join")) {
                      String[] parts = msg.split(":");
                      String   user  = parts[1];
                      String   group = parts[2];
                      return P.p(members.update(group, userSet -> userSet.insert(user), Set.single(Ord.stringOrd, user)), Option.<String>none());
                  } else if (msg.startsWith("leave")) {
                      String[] parts = msg.split(":");
                      String   user  = parts[1];
                      String   group = parts[2];
                      return P.p(members.update(group, userSet -> userSet.delete(user))._2(), Option.<String>none());
                  } else if (msg.startsWith("message")) {
                      String[] parts   = msg.split(":");
                      String   groupId = parts[2];
                      List<String> mentions =
                        members.get(groupId).orSome(Set.empty(Ord.stringOrd)).toList()
                          .filter(member ->
                            parts[3].contains("@" + member + " "));

                      if (mentions.isNotEmpty()) {
                          return P.p(members, Option.some("mention:" + mentions.head() + ":" + groupId));
                      } else
                          return P.p(members, Option.<String>none());
                  } else {
                      return P.p(members, Option.<String>none());
                  }
              })
              .mapOption(o -> o)
              .apply(out -> Util.println(out).thenJust(out))
              .map(ProducerRecord.toMessage(TopicName.solution("jalla")));

        ws3.run(
          app
        ).await(Duration.ofSeconds(5));
    }

}
