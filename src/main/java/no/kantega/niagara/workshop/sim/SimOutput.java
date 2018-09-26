package no.kantega.niagara.workshop.sim;

import fj.P2;
import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;
import no.kantega.niagara.workshop.server.Progress;

import static fj.Equal.stringEqual;

public interface SimOutput {


  String asString();

  Option<Progress> task();

  class Create implements SimOutput {

    final Person person;
    final Group  group;

    Create(Person person, Group group) {
      this.person = person;
      this.group = group;
    }

    @Override
    public String asString() {
      return "create:" + person.id + ":" + group.id;
    }

    @Override
    public Option<Progress> task() {
      return Option.none();
    }
  }

  class Join implements SimOutput {
    final Person person;
    final Group  group;

    Join(Person person, Group group) {
      this.person = person;
      this.group = group;
    }

    @Override
    public String asString() {
      return "join:" + person.id + ":" + group.id;
    }

    @Override
    public Option<Progress> task() {
      return Option.none();
    }
  }

  class Leave implements SimOutput {

    final Person person;
    final Group  group;

    Leave(Person person, Group group) {
      this.person = person;
      this.group = group;
    }

    @Override
    public String asString() {
      return "leave:" + person.id + ":" + group.id;
    }

    @Override
    public Option<Progress> task() {
      return Option.none();
    }
  }

  class Post implements SimOutput {

    final Person       author;
    final Group        group;
    final String       text;
    final List<Person> mention;
    final String       expectMsg;

    Post(Person author, Group group, String text, List<Person> mention) {
      this.author = author;
      this.group = group;
      this.text = text;
      this.mention = mention;
      this.expectMsg = mention.isEmpty() ? "" : "mention:" + mention.head().id + ":" + group.id;
    }

    public static SimOutput post(Person author, Group group, String text, List<Person> mention){
      return new Post(author,group,text,mention);
    }

    @Override
    public String asString() {
      return "message:" + author.id + ":" + group.id + ":" + text;
    }

    @Override
    public Option<Progress> task() {
      return
        mention.isEmpty() ?
        Option.none() :
        Option.some(Progress.expectWithFailMsg(
          input -> stringEqual.eq(input.message, expectMsg),
          input -> "Was: " + input.message + ",  expecting:" + expectMsg,
          input -> Progress.done()
        ));
    }
  }

  class Logon implements SimOutput {
    final Person                  person;
    final TreeMap<Group, Integer> offlineMentions;
    final String                  expectMsg;

    public Logon(Person person, TreeMap<Group, Integer> offlineMentions) {
      this.person =
        person;

      this.offlineMentions =
        offlineMentions;

      List<P2<Group, Integer>> list =
        offlineMentions.toList();

      this.expectMsg =
        "update:" + person.id + list.foldLeft((str, pair) -> str + ":" + pair._1().id + ";" + pair._2(), "");
    }

    @Override
    public String asString() {
      return "online:" + person.id;
    }

    @Override
    public Option<Progress> task() {
      return
        offlineMentions.isEmpty() ?
        Option.none() :
        Option.some(Progress.expectWithFailMsg(
          input -> stringEqual.eq(input.message, expectMsg),
          input -> "Was: " + input.message + ",  expecting:" + expectMsg,
          input -> Progress.done()
        ));
    }
  }

  class Logoff implements SimOutput {

    final Person person;

    Logoff(Person person) {
      this.person = person;
    }

    @Override
    public String asString() {
      return "offline:" + person.id;
    }

    @Override
    public Option<Progress> task() {
      return Option.none();
    }
  }
}
