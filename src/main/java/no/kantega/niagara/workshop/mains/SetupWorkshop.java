package no.kantega.niagara.workshop.mains;

import fj.data.Stream;
import no.kantega.niagara.broker.ProducerRecord;
import no.kantega.niagara.broker.TopicName;
import no.kantega.niagara.workshop.Client;
import no.kantega.niagara.workshop.Util;
import no.kantega.niagara.workshop.server.Sim;
import no.kantega.niagara.workshop.server.WorkshopTasks;
import org.kantega.niagara.Sources;

import java.time.Duration;
import java.util.Random;

public class SetupWorkshop {

    public static void main(String[] args) {
       /* Client.WS ws =
          Client.websocket("localhost", 8080);

        ws.run( Sources
          .fromIterable(WorkshopTasks.echoStrings)
          .map(ProducerRecord.toMessage(TopicName.echo())));
          */


        Random rand = new Random(2);

        Sim sim = Sim.newSimulation(rand);


        Sources.fromIterable(Stream.range(0,500)).mapWithState(sim,(s,n)->s.next(rand)).flatten(i->i).apply(Util::println).toTask().execute().await(Duration.ofSeconds(10));


    }


}
