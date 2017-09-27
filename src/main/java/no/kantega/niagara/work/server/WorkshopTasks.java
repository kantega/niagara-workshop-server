package no.kantega.niagara.work.server;

import fj.data.Seq;

import java.util.Random;

import static no.kantega.niagara.work.server.Progress.*;

public class WorkshopTasks {

    public static final Progress tasks =
      emit("Velkommen til workshop, send nicket ditt til url bla bla, litt om task og sånn. mellom 4 og 20 tegn")
        .then(expect(nickMsg -> nickMsg.length() > 4 && nickMsg.length() < 21, nickMsg -> emit(succ("Velkommen " + nickMsg))
          .then(emit("Lag en stream som sender nicket ditt med Stream.value og sånn"))
          .then(expect(msg -> msg.equals(nickMsg), msg -> emit(randSucc())))
          .then(emit("Bruk map til endre navnet til UPPERCASE"))
          .then(expect(msg -> msg.equals(nickMsg.toUpperCase()), msg -> emit(randSucc())))
          .then(emit("Bruk flatten til å sende en og en bokstav"))
          .then(awaitLettersIn(nickMsg))
          .then(emit(randSucc()))));


    static Random rand = new Random(System.currentTimeMillis());

    public static String succ(String msg) {
        return randSucc() + " " + msg;
    }

    public static String randSucc() {
        Seq<String> randGreet = Seq.arraySeq("Bra jobba!", "Nais!", "Phett!", "Nydelig!", "Dette var bra!");

        return randGreet.index(rand.nextInt(randGreet.length()));
    }

    static Progress awaitLettersIn(String string) {
        if (string.length() == 1)
            return expect(msg -> msg.equals(string), msg -> done());
        else
            return expect(msg -> msg.length() == 1 && string.startsWith(msg), msg -> awaitLettersIn(string.substring(1)));
    }

}
