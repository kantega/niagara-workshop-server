package no.kantega.niagara.workshop.sim;

import fj.P2;
import fj.data.*;
import no.kantega.niagara.stream.Sources;
import no.kantega.niagara.workshop.Util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static fj.P.p;
import static fj.data.List.*;
import static fj.data.TreeMap.empty;
import static fj.data.TreeMap.treeMap;
import static no.kantega.niagara.workshop.sim.Generator.*;
import static no.kantega.niagara.workshop.sim.Sim.*;
import static no.kantega.niagara.workshop.sim.SimOutput.*;
import static no.kantega.niagara.workshop.sim.SimOutput.Post.post;

public class LogOnOffSim {

  static final AtomicLong personCounter = new AtomicLong();
  static final AtomicLong groupCounter  = new AtomicLong();


  public final Set<Person>                              persons;
  public final Set<Group>                               groups;
  public final Set<Membership>                          memberships;
  public final Set<Person>                              onlineUsers;
  public final TreeMap<Person, TreeMap<Group, Integer>> offlineMentions;

  public LogOnOffSim(
    Set<Person> persons,
    Set<Group> groups,
    Set<Membership> memberships,
    Set<Person> onlineUsers,
    TreeMap<Person, TreeMap<Group, Integer>> offlineMentions) {

    this.persons = persons;
    this.groups = groups;
    this.memberships = memberships;
    this.onlineUsers = onlineUsers;
    this.offlineMentions = offlineMentions;
  }

  public static void main(String[] args) {
    Random rand = new Random(6);
    Sources.toList(Sources.fromIterable(Stream.range(0, 500))
      .mapWithState(LogOnOffSim.newSimulation(rand), (s, n) -> s.next(rand))
      .flatten(i -> i)
      .apply(out -> Util.println(out.asString()))
    )
      .execute();
  }

  public static LogOnOffSim newSimulation(Random random) {
    int         numOfPersons = random.nextInt(10) + 10;
    Set<Person> persons      = Set.iterableSet(personOrd, Stream.range(0, numOfPersons).map(i -> new Person("u" + personCounter.incrementAndGet())));
    Set<Group>  groups       = Set.empty(groupOrd);
    return new LogOnOffSim(persons, groups, Set.empty(membershipOrd), Set.empty(personOrd), empty(personOrd));
  }


  public P2<LogOnOffSim, List<SimOutput>> next(Random rand) {

    Generator<P2<LogOnOffSim, List<SimOutput>>> gen =

      oneIn(persons).bind(person -> {

        Set<Group> isMemberOf = membershipsFor(memberships, person);

        Generator<P2<LogOnOffSim, List<SimOutput>>> g =
          onlineUsers.member(person) ?
          orWeight(
            2, join(person, isMemberOf),
            1, logoff(person),
            isMemberOf.size(), orWeight(1, leave(person, isMemberOf), 9, write(person))) :
          logon(person);

        return g;
      });

    return gen.gen(rand);
  }


  Generator<P2<LogOnOffSim, List<SimOutput>>> join(Person p, Set<Group> memberOf) {
    return rand -> {

      Set<Group> available =
        groups.minus(memberOf);

      boolean create = orWeight(groups.size(), always(false), 5, always(true)).gen(rand);

      if (create || available.isEmpty()) {
        Group           group         = new Group("g" + groupCounter.incrementAndGet());
        Set<Group>      newGroups     = groups.insert(group);
        Set<Membership> newMembership = memberships.insert(new Membership(p, group));
        return p(
          new LogOnOffSim(persons, newGroups, newMembership, onlineUsers, offlineMentions),
          list(new Create(p, group), new Join(p, group))
        );
      } else {
        Group           group         = oneIn(available).gen(rand);
        Set<Membership> newMembership = memberships.insert(new Membership(p, group));
        return p(
          new LogOnOffSim(persons, groups, newMembership, onlineUsers, offlineMentions),
          list(new Join(p, group))
        );
      }
    };
  }

  Generator<P2<LogOnOffSim, List<SimOutput>>> leave(Person p, Set<Group> memberOf) {
    return oneIn(memberOf).map(group -> {
      Set<Membership> newMembership = memberships.delete(new Membership(p, group));
      return p(
        new LogOnOffSim(persons, groups, newMembership, onlineUsers, offlineMentions),
        list(new Leave(p, group))
      );
    });
  }

  Generator<P2<LogOnOffSim, List<SimOutput>>> write(Person person) {
    return
      oneIn(membershipsFor(memberships, person))
        .bind(group ->
          oneIn(membersOf(memberships, group)).bind(m ->
            randSentence.bind(s ->
              orWeight(
                1, always(post(person, group, s, nil()))
                  .map(out -> p(LogOnOffSim.this, single(out))),
                1, oneIn(notMembersOf(group)).bind(notMember -> writeNoMention(group, person, s, notMember))
                  .map(out -> p(LogOnOffSim.this, single(out))),
                1, writeMention(group, person, s, m)
              ))
          ));
  }

  Generator<P2<LogOnOffSim, List<SimOutput>>> logon(Person person) {
    TreeMap<Group, Integer> medntions =
      offlineMentions.get(person).orSome(empty(groupOrd));

    return
      always(p(
        new LogOnOffSim(persons, groups, memberships, onlineUsers.insert(person), offlineMentions),
        list(new Logon(person, medntions))
      ));
  }

  Generator<P2<LogOnOffSim, List<SimOutput>>> logoff(Person person) {

    return
      always(p(
        new LogOnOffSim(persons, groups, memberships, onlineUsers.delete(person), offlineMentions.delete(person)),
        list(new Logoff(person))
      ));
  }

  private Set<Person> notMembersOf(Group group) {
    return persons.minus(membersOf(memberships, group));
  }

  Generator<P2<LogOnOffSim, List<SimOutput>>> writeMention(Group group, Person author, String sentence, Person person) {
    return rand -> {
      Seq<String> splitted = Seq.arraySeq(sentence.split(" "));
      Seq<String> inserted = splitted.insert(rand.nextInt(splitted.length()), "@" + person.id);
      String      text     = inserted.tail().foldLeft((sum, word) -> sum + " " + word, inserted.head());
      TreeMap<Person, TreeMap<Group, Integer>> updatedOLMentions =
        onlineUsers.member(person) ?
        offlineMentions :
        offlineMentions.update(person, tm -> tm.update(group, count -> count + 1, 1), treeMap(groupOrd, p(group, 1)));

      return p(
        new LogOnOffSim(persons, groups, memberships, onlineUsers, updatedOLMentions),
        single(new Post(author, group, text, single(person)))
      );
    };
  }

  Generator<SimOutput> writeNoMention(Group group, Person author, String sentence, Person person) {
    return rand -> {
      Seq<String> splitted = Seq.arraySeq(sentence.split(" "));
      Seq<String> inserted = splitted.insert(rand.nextInt(splitted.length()), "@" + person.id);
      String      text     = inserted.tail().foldLeft((sum, word) -> sum + " " + word, inserted.head());
      return new Post(author, group, text, nil());
    };
  }


}
