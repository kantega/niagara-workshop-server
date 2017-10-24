package no.kantega.niagara.work.server;

import fj.data.Seq;

import java.util.Random;

import static no.kantega.niagara.work.server.Progress.*;

public class WorkshopTasks {

    public static final Progress tasks =
      emit("Velkommen til workshop, send nicket ditt til url bla bla, litt om task og sånn. Over 3 og under 20 tegn")
        .then(expectWithFailMsg(
          nickMsg ->
            nickMsg.trim().length() > 3 && nickMsg.length() < 20, msg -> "Nicket må være mer enn 3 og mindre enn 20 tegn",
          nickMsg ->
            emit(succ("Velkommen " + nickMsg))
              .then(emit("Lag en stream som sender nicket ditt med Stream.value og sånn"))
              .then(expectWithFailMsg(msg -> msg.equals(nickMsg), msg -> "Forventet " + nickMsg + ", men fikk " + msg, msg -> emit(randSucc())))
              .then(emit("Bruk _map_ til endre navnet til UPPERCASE"))
              .then(expectWithFailMsg(msg -> msg.equals(nickMsg.toUpperCase()), msg -> "Forventet " + nickMsg.toUpperCase() + ", men fikk " + msg, msg -> emit(randSucc())))
              .then(emit("Bruk _flatten_ til å sende ett tegn om gangen"))
              .then(awaitLettersIn(nickMsg.toUpperCase()))
              .then(emit(randSucc()))
              .then(emit("To Sources kan settes sammen slik at den andre starter når den første slutter med _append()_. Lag en stream som sender nicket to ganger etterhverandre, bokstav for bokstav, i lowercase"))
              .then(awaitLettersIn(nickMsg))
              .then(awaitLettersIn(nickMsg))
              .then(emit(randSucc()).then(emit("Done")))
        ));


    static Random rand = new Random(System.currentTimeMillis());

    public static String succ(String msg) {
        return randSucc() + " " + msg;
    }

    public static String randSucc() {
        Seq<String> randGreet = Seq.arraySeq("Bra jobba!", "Nais!", "Phett!", "Nydelig!", "Dette var bra!", "Sweet.", "Herlig.");

        return randGreet.index(rand.nextInt(randGreet.length()));
    }

    public static String randFail() {
        Seq<String> randGreet = Seq.arraySeq("Hmm....", "Ikke helt.", "Tjaa...", "Naai...", "Prøv en gang til...");

        return randGreet.index(rand.nextInt(randGreet.length()));
    }

    static Progress awaitLettersIn(String string) {
        if (string.length() == 1)
            return expect(msg -> msg.equals(string), msg -> done());
        else
            return expectWithFailMsg(
              msg -> msg.length() == 1 && string.startsWith(msg),
              msg -> randFail() + " forventer tegnet " + string.substring(0, 1) + " men fikk " + msg,
              msg -> awaitLettersIn(string.substring(1)));
    }

}
