package no.kantega.niagara.workshop.mains;

import fj.Ord;
import fj.P;
import fj.data.List;
import fj.data.Option;
import fj.data.Set;
import fj.data.TreeMap;
import no.kantega.niagara.stream.Sources;
import no.kantega.niagara.stream.Stream;
import no.kantega.niagara.workshop.Client;
import no.kantega.niagara.workshop.Util;
import org.junit.Test;

import java.util.Arrays;

import static no.kantega.niagara.broker.ProducerRecord.*;
import static no.kantega.niagara.broker.TopicName.*;

public class TestSolver {


    public final String id = "jalla";

    public static Client.WS ws =
      Client.websocket("10.80.8.187", 8080);

    @Test
    public void task3() {
        Client.run(ws, Sources.emitOne("start").map(toMessage(start(id))));
    }

    @Test
    public void task4() {
        Client.run(ws, id, Sources.emitOne("atle"));
    }

    @Test
    public void task5() {
        Client.run(ws, id,
          Sources.emitOne("atle")
            .map(String::toUpperCase)
        );
    }

    @Test
    public void task6() {
        Client.run(ws, id,
          Sources.emitOne("atle")
            .map(String::toUpperCase)
            .flatten(n -> Arrays.asList(n.split("")))
        );
    }

    @Test
    public void task7() {
        Client.run(ws, id,
          Sources.emitOne("atle")
            .append(() -> Sources.emitOne("atle"))
            .flatten(n -> Arrays.asList(n.split("")))
        );
    }


    @Test
    public void task8() throws InterruptedException {
        Stream<String, String> app =
          incoming -> incoming;
        Client.run(ws, id, "/echo", app);
    }

    @Test
    public void task9() throws InterruptedException {
        Stream<String, String> app =
          incoming -> incoming.foldLeft("", (cum, in) -> cum + in);
        Client.run(ws, id, "/echo", app);
    }


    @Test
    public void task10() throws InterruptedException {
        Stream<String, String> app =
          incoming ->
            incoming
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
              .apply(out -> Util.println(out).thenJust(out));

        Client.run(ws, id, "/memberships", app);
    }

}
