package no.kantega.niagara.workshop.server;

import fj.Equal;
import fj.F;
import fj.data.List;
import fj.data.Seq;
import no.kantega.niagara.workshop.mains.SetupWorkshop;

import java.util.Random;
import java.util.function.Supplier;

import static no.kantega.niagara.workshop.server.Progress.*;

public class WorkshopTasks {

  private static Random rand = new Random(System.currentTimeMillis());

  private static F<String, String> task1desc = id ->
    "start:Hei og velkommen til workshop om eventdreven applikasjonsutvikling.\n" +
      "I denne workshoppen går oppgavene ut på å sende `ProducerRecord` meldinger " +
      "(og etterhvert motta `ConsumerRecord` meldinger) til " +
      "en meldingsbroker. En event-drevet arkitektur er ikke avhengig av bestemte meldignestyper, " +
      "men de aller fleste rammeverk krever en viss formening " +
      "om topics eller keys , og man vil i praksis måtte forholde seg til dette. " +
      "Her har vi lagt dette inn i ProducerRecord.\n" +
      "I en event-drevet arkitektur er det stort sett en datatype man må kjenne godt til: " +
      "En type som representere en (uendelig) strøm av data " +
      "I denne workshoppen kan du f.eks. bruke _Niagara_ til å modellere datastrømmer og " +
      "her kalles en slik datakilde for `Source`. Den omkapsler det faktum at man ikke vet når " +
      "en verdi oppstår (den abstrahere vekk tidsaspektet). " +
      "Du kan bruke rammeverket du selv vil, men det bør abstrahere vekk tid. \n" +
      "I først oppgave skal du lage en `Source` som emitter en " +
      "melding som inneholder teamets navn.\n" +
      "Navnet må være:\n" +
      " * mer enn 3 tegn\n" +
      " * under 20 tegn\n" +
      " * inneholde små bokstaver\n\n" +
      "Send meldingen ved å bruke `Ws ws = Client.websocket(172.16.0.168, 8080, [SubscribeTo.replayAndSubscribeTo(/memberships)])` og så `ws.run()` eller å sende meldingen direkte til " +
      "ws://172.16.0.168:8080/ws/ med formatet {topic:/solution/" + id + ", msg: _mld_ }";

  private static String task2desc =
    "task:Man kan endre output til en `Source` meldig for melding med `map()`. " +
      "Bruk dette til å endre datakilden din til å sende ut teamnavnet i UPPERCASE. " +
      "Man kan også bruke `map()` for å først lage " +
      "`Stream<String>` og så bruke `map()` til å lage `ProducerRecord` meldinger\n" +
      "(Hint: Stream.value(nick).map(s-> ProducerRecord.message(TopicName.solution(name),s).";

  private static String task3desc =
    "task:Ofte har man bruk for å sende ut (emitte) flere meldinger for hver medling en source produserer. `Source` " +
      " har en metode `flatten()` som man kan " +
      " bruke for å oppnå dette. Prøv dette i praksis ved å lage en kilde med bokstaver av kilden som sender navnet.";


  private static String task4desc =
    "task:En nyttig ting med datakilder er at man kan sette dem sammen slik at en starter å emitte " +
      "meldinger når den forrige er ferdig. " +
      "Dette kan brukes for å sette sammen ulike kilder, men også implementere retry, failover osv... " +
      "I Niagara bruker man `append()` for å " +
      "å sette sammen streams. Det finnes tilsvarende i start sett alle biblioteker. \n" +
      "I denne oppgaven skal vi gjøre det enkelt, " +
      "send nicket,  boksktav for bokstav, to ganger etter hverandre.";


  private static String task5desc =
    "task:Nå som dere har fått lekt dere litt med api'et, er det på tide å se litt større på det. \n" +
      "En event-drevet applikasjon er egentlig et program som har events som input, og events som output. " +
      "Ganske komplisert ikkesant? " +
      "En tommelfingerregel er at det kun produseres output basert på input " +
      "og at output er et asynkront resultat av input. " +
      "\n" +
      "Neste oppgave går ut på å lage et program som bare echoer input til output. " +
      "Niagara har en datatype for programmer som har en datakilde " +
      "som input, og en som output: `Stream`. Bruk `Client.websocket(host,port,topic[])` og abonnér på topic 'echo'. " +
      "Bruk så `Client.WS.run(Stream<A,B>)` " +
      "for å kjøre streamen.";

  public static String tasc6desc =
    "task:Nå skal vi implementere et materialized views som ble nevnt innledningsvis. " +
      "Lag en left fold av samme source som " +
      "forrige oppgave. Implementer " +
      "en fold S -> I -> S, (forrige output -> input -> output) der output er _forrige output + input_";

  public static String tasc7desc =
    "task:Nå kommer endelig en reell oppgave:\n" +
      "Dere skal nå lage en server som tar i mot en strøm av meldinger fra et ukjent men stort antall brukere. " +
      "Meldingene har følgende format:\n" +
      "`join:[brukerid]:[gruppeId]`\n" +
      "`leave:[brukerId]:[gruppeId]`\n" +
      "`create:[gruppeId]`\n" +
      "`message:[userId]:[groupId]:[text]` - der `text` kan inneholde en _mention_, i dette tilfellet @[brukerId]\n" +
      "\n" +
      "Dere skal lage en applikasjon som sender et varsel til en bruker når han blir _mentioned_ i en " +
      "gruppe som _vedkommende er medlem i_ " +
      "(man kan blir mentioned " +
      "i grupper man ikke er medlem i også, de skal ignoreres).\n" +
      "Meldingen skal ha format `mention:[brukerId]:[gruppeId]`\n" +
      "Meldingene ligger i topic /memberships";

  public static String tasc8desc =
    "task:Vokabularet fra forrige oppgave utvides nå med meldinger om at en bruker logger på og av. " +
      "Når en bruker logger på skal vedkommende ha tilsent en oppdatert status på mentions som har skjedd siden sist." +
      "Etter denne initielle meldingen skal det sendes oppdateringer med mentions helt til brukeren logger av." +
      "Altså nye inkomne meldinger:\n" +
      "online:<userId>\n" +
      "offline:<userId>\n" +
      "Og nye utgående:\n" +
      "update:<userId>:<groupId1>;<mentions1>: ... :<groupIdN>;<mentionsN> (Nb: semikolon mellom gruppeid " +
      "og antall mentions, sortert alfabetisk på gruppeid)";

  public static String finish =
    "Bra jobba, nå er workshoppen ferdig. Men hvis du har tid må du gjerne fortsette å jobbe med enda flere problemstillinger og utfordringer";

  public static final List<String> echoStrings =
    List.arrayList("e", "c", "h", "oo", "oo");

  public static final List<String> foldStrings =
    List.arrayList("e", "ec", "ech", "echoo", "echoooo");

  private final static Seq<String> yays =
    Seq
      .arraySeq("Bra jobba!", "Nais!", "Phett!", "Nydelig!", "Dette var bra!", "Sweet.", "Herlig.", "Kult!")
      .map(str -> str + "\n");

  private final static Seq<String> nays =
    Seq
      .arraySeq("Hmm...", "Ikke heeelt...", "Tjaa...", "Naai...", "Prøv en gang til...")
      .map(str -> str + "\n");


  private static String randSucc() {
    return yays.index(rand.nextInt(yays.length()));
  }

  private static String succ(String msg) {
    return randSucc() + " " + msg;
  }

  private static String randFail() {
    return "fail:" + nays.index(rand.nextInt(nays.length()));
  }

  public static final F<String, Progress> tasks =
    id -> emit(task1desc.f(id))
      .then(expectWithFailMsg(
        nickMsg ->
          nickMsg.message.trim().length() > 3 && nickMsg.message.length() < 20, msg -> randFail() + " Nicket må være mer enn 3 og mindre enn 20 tegn",
        nickMsg ->
          emit("Velkommen " + nickMsg.message)
            .then(emit(task2desc))
            .then(expectWithFailMsg(msg -> msg.message.equals(nickMsg.message.toUpperCase()), msg -> randFail() + " Forventet " + nickMsg.message.toUpperCase() + ", men fikk " + msg.message, msg -> emit(randSucc())))
            .then(emit(task3desc))
            .then(awaitLettersIn(nickMsg.message.toUpperCase()))
            .then(emit(randSucc()))
            .then(emit(task4desc))
            .then(awaitLettersIn(nickMsg.message).then(awaitLettersIn(nickMsg.message)))
            .then(emit(randSucc()))
            .then(emit(task5desc))
            .then(awaitMessagesReset(echoStrings))
            .then(emit(randSucc()))
            .then(emit(tasc6desc))
            .then(awaitMessagesReset(foldStrings))
            .then(emit(randSucc()))
            .then(emit(tasc7desc))
            .then(SetupWorkshop.membershipProgress)
            .then(emit(randSucc()))
            .then(emit(tasc8desc))
            .then(SetupWorkshop.logOnOffProgress)
            .then(emit(randSucc()))
            .then(emit(finish))
      ));


  static Progress awaitLettersIn(String string) {
    if (string.length() == 1)
      return expect(msg -> msg.message.equals(string), msg -> done());
    else
      return expectWithFailMsg(
        msg -> msg.message.length() == 1 && string.startsWith(msg.message),
        msg -> randFail() + " forventer tegnet " + string.substring(0, 1) + " men fikk " + msg.message,
        msg -> awaitLettersIn(string.substring(1)));
  }

  static Progress awaitMessagesReset(List<String> messages) {
    return awaitMessagesRec(() -> awaitMessagesReset(messages), messages);
  }

  static Progress awaitMessagesRec(Supplier<Progress> reset, List<String> messages) {
    if (messages.isEmpty())
      return done();
    else {
      return expect(
        msg -> Equal.stringEqual.eq(msg.message, messages.head()),
        msg -> emit("fail:Expecting " + messages.head() + ", but got " + msg.message + ". Start again").then(reset.get()),
        msg -> awaitMessagesRec(reset, messages.tail())
      );
    }
  }

}
