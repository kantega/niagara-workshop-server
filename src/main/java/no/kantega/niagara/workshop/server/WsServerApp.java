package no.kantega.niagara.workshop.server;

import no.kantega.niagara.broker.ConsumerRecord;
import no.kantega.niagara.broker.ProducerRecord;
import no.kantega.niagara.stream.Source;
import no.kantega.niagara.stream.Stream;
import no.kantega.niagara.workshop.Util;

import static no.kantega.niagara.workshop.Util.println;

public class WsServerApp implements Stream<ConsumerRecord, ProducerRecord> {

    public Source<ProducerRecord> apply(Source<ConsumerRecord> incoming) {
        return

          incoming
            .apply(inc -> println("Incoming:" + inc.toString()).map(u -> inc))
            .mapWithMealy(ApplicationState.newEmpty())
            .flatten(i -> i)
            .apply(out -> println("Outgoing:" + out.toString()).map(u -> out))
            .onClose(Util.println("Closed webserver stream"));
    }

}
