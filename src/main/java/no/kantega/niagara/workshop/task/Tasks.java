package no.kantega.niagara.workshop.task;

import fj.Bottom;
import no.kantega.niagara.workshop.Client;
import org.junit.Test;
import org.kantega.niagara.Source;
import org.kantega.niagara.Stream;

public class Tasks {


    private static final String yourId =
      toDo(); //TODO  <<-- *** sett inn din id her (slik som det står på websiden) ***

    private static Client.WS connection =
      Client.websocket("172.16.0.168", 8080);


    @Test
    public void task1to5() {
        //TODO
        //Etter at dere har BYTTET UT ID OVER, så
        //kan dere bruke "Sources" for å lage en source som emitter (=sender)
        //team navnet
        Source<String> source =
          toDo();

        //Kjør appen
        Client.run(
          connection,
          yourId,
          source);
    }

    @Test
    public void task5to6() {
        //TODO
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
        //TODO
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
