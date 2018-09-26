package no.kantega.niagara.workshop.mains;

import fj.data.*;
import no.kantega.niagara.broker.ProducerRecord;
import no.kantega.niagara.broker.TopicName;
import no.kantega.niagara.stream.Sources;
import no.kantega.niagara.workshop.Client;
import no.kantega.niagara.workshop.server.Progress;
import no.kantega.niagara.workshop.sim.LogOnOffSim;
import no.kantega.niagara.workshop.sim.MembershipSim;
import no.kantega.niagara.workshop.server.WorkshopTasks;
import no.kantega.niagara.workshop.sim.SimOutput;

import java.time.Duration;
import java.util.Random;

public class SetupWorkshop {
  public static final Random rand =
    new Random(2);

  public static final MembershipSim MEMBERSHIP_SIM =
    MembershipSim.newSimulation(rand);

  public static final LogOnOffSim logOnOffSim =
    LogOnOffSim.newSimulation(rand);

  public static final List<SimOutput> simOutputSource =
    Sources.toList(Sources.fromIterable(Stream.range(0, 500))
      .mapWithState(MEMBERSHIP_SIM, (s, n) -> s.next(rand))
      .flatten(i -> i))
      .execute()
      .await(Duration.ofSeconds(5))
      .orThrow();

  public static final List<SimOutput> logOnOffOutputSource =
    Sources.toList(Sources.fromIterable(Stream.range(0, 500))
      .mapWithState(logOnOffSim, (s, n) -> s.next(rand))
      .flatten(i -> i))
      .execute()
      .await(Duration.ofSeconds(5))
      .orThrow();

  public static final Progress membershipProgress =
    Option.somes(simOutputSource.map(SimOutput::task)).foldLeft(Progress::then, Progress.done());

  public static final Progress logOnOffProgress =
    Option.somes(logOnOffOutputSource.map(SimOutput::task)).foldLeft(Progress::then, Progress.done());


  public static void main(String[] args) {
    Client.WS ws =
      Client.websocket(Settings.brokerIp, 8080);

    Client.run(ws,Sources
      .fromIterable(WorkshopTasks.echoStrings)
      .map(ProducerRecord.toMessage(TopicName.echo()))
      .append(() ->
        Sources.fromIterable(simOutputSource)
          .map(SimOutput::asString)
          .map(ProducerRecord.toMessage(TopicName.memberships())))
      .append(() ->
        Sources.fromIterable(logOnOffOutputSource)
          .map(SimOutput::asString)
          .map(ProducerRecord.toMessage(TopicName.logonOff()))
      ));


  }


}
