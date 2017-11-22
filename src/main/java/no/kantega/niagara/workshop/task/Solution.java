package no.kantega.niagara.workshop.task;

import fj.Bottom;
import no.kantega.niagara.workshop.Client;
import org.junit.Test;
import org.kantega.niagara.Source;
import org.kantega.niagara.Stream;

public class Solution {


    private static final String yourId =
      toDo(); //TODO  <<-- *** sett inn din id her (slik som det står på websiden) ***

    private static Client.WS connection =
      Client.websocket("10.80.8.187", 8080);


    //Bruk denne metoden som mal for oppgave 1 til 5.
    @Test
    public void task1to4() {
        //Bruk "Sources" for å lage en source som emitter (=sender) teamnavnet deres
        Source<String> source =
          toDo();

        //Kjør appen
        Client.run(
          connection, //Websocket connection wrapper
          yourId, // Team id
          source //Sourcen deres
        );
    }

    //Bruk denne til oppgave 5 og 6.
    @Test
    public void task5to6() {
        //Etter at dere har BYTTET UT ID OVER, så
        //team navnet
        Stream<String, String> stream =
          incoming -> toDo();

        //Kjør appen
        Client.run(
          connection, //Din connection
          yourId, //Id
          "/echo", //Appen din mottar meldinger fra denne topic'en
          stream //Din app
        );
    }

    @Test
    public void task7() {
        //Etter at dere har BYTTET UT ID OVER, så
        //team navnet
        Stream<String, String> stream =
          incoming -> toDo();

        //Kjør appen
        Client.run(connection, yourId, "/memberships", stream);
    }

    static <A> A toDo() {
        throw Bottom.undefined();
    }
}
