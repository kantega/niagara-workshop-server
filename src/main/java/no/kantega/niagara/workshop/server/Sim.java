package no.kantega.niagara.workshop.server;

import fj.F;
import fj.Ord;
import fj.P;
import fj.P2;
import fj.data.List;
import fj.data.Seq;
import fj.data.Set;
import fj.data.Stream;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class Sim {
    static final AtomicLong personCounter = new AtomicLong();
    static final AtomicLong groupCounter  = new AtomicLong();

    static final Ord<Group>      groupOrd      = Ord.stringOrd.contramap(g -> g.id);
    static final Ord<Person>     personOrd     = Ord.stringOrd.contramap(p -> p.id);
    static final Ord<Membership> membershipOrd = Ord.p2Ord(personOrd, groupOrd).contramap(m -> P.p(m.person, m.group));

    public final Set<Person>     persons;
    public final Set<Group>      groups;
    public final Set<Membership> memberships;

    public Sim(
      Set<Person> persons,
      Set<Group> groups, Set<Membership> memberships) {

        this.persons = persons;
        this.groups = groups;
        this.memberships = memberships;
    }

    public static Sim newSimulation(Random random) {
        int         numOfPersons = random.nextInt(10) + 10;
        Set<Person> persons      = Set.iterableSet(personOrd, Stream.range(0, numOfPersons).map(i -> new Person("u" + personCounter.incrementAndGet())));
        Set<Group>  groups       = Set.empty(groupOrd);
        return new Sim(persons, groups, Set.empty(membershipOrd));
    }


    public P2<Sim, List<String>> next(Random rand) {


        Generator<P2<Sim, List<String>>> gen =
          oneIn(persons).bind(person -> {

              Set<Group> isMemberOf = membershipsFor(person);

              Generator<P2<Sim, List<String>>> g =
                orWeight(
                  1, join(person, isMemberOf),
                  isMemberOf.size(), orWeight(1, leave(person, isMemberOf), 9, write(person)));

              return g;
          });

        return gen.gen(rand);
    }

    static class Person {

        public final String id;

        Person(String id) {
            this.id = id;
        }
    }

    static class Group {

        public final String id;

        Group(String id) {
            this.id = id;
        }
    }

    static class Membership {
        public final Person person;
        public final Group  group;

        Membership(Person person, Group group) {
            this.person = person;
            this.group = group;
        }
    }

    Generator<P2<Sim, List<String>>> join(Person p, Set<Group> memberOf) {
        return rand -> {

            Set<Group> available =
              groups.minus(memberOf);

            boolean create = orWeight(groups.size(), always(false), 5, always(true)).gen(rand);

            if (create || available.isEmpty()) {
                Group           group         = new Group("g" + groupCounter.incrementAndGet());
                Set<Group>      newGroups     = groups.insert(group);
                Set<Membership> newMembership = memberships.insert(new Membership(p, group));
                return P.p(
                  new Sim(persons, newGroups, newMembership),
                  List.list("create:" + group.id, "join:" + p.id + ":" + group.id)
                );
            } else {

                Group           group         = oneIn(available).gen(rand);
                Set<Membership> newMembership = memberships.insert(new Membership(p, group));
                return P.p(
                  new Sim(persons, groups, newMembership),
                  List.list("join:" + p.id + ":" + group.id)
                );
            }
        };
    }

    Generator<P2<Sim, List<String>>> leave(Person p, Set<Group> memberOf) {
        return oneIn(memberOf).map(group -> {
            Set<Membership> newMembership = memberships.delete(new Membership(p, group));
            return P.p(
              new Sim(persons, groups, newMembership),
              List.list("leave:" + p.id + ":" + group.id)
            );
        });
    }

    Generator<P2<Sim, List<String>>> write(Person person) {
        return
          oneIn(membershipsFor(person))
            .bind(group -> oneIn(membersOf(group)).bind(m -> randSentence
              .or(randSentence.bind(s -> writeMention(s, m)).or(randSentence.bind(s->oneIn(persons.minus(membersOf(group))).bind(p->writeMention(s,p)))))
              .map(sentence -> {
                  return P.p(Sim.this, List.single("message:" +person.id+":"+ sentence));
              })));
    }

    Generator<String> writeMention(String sentence, Person person) {
        return rand -> {
            Seq<String> splitted = Seq.arraySeq(sentence.split(" "));
            Seq<String> inserted = splitted.insert(rand.nextInt(splitted.length()), "@"+person.id);
            return inserted.tail().foldLeft((sum, word) -> sum + " " + word, inserted.head());
        };
    }

    Set<Person> membersOf(Group group) {
        return memberships.filter(m -> m.group.id.equals(group.id)).map(personOrd, m -> m.person);
    }

    Set<Group> membershipsFor(Person person) {
        return memberships.filter(m -> m.person.id.equals(person.id)).map(groupOrd, m -> m.group);
    }

    interface Generator<A> {

        A gen(Random rand);

        default Generator<A> or(Generator<A> other) {
            return orWeight(1, this, 1, other);
        }

        default <B> Generator<B> map(F<A, B> f) {
            return rand -> f.f(gen(rand));
        }

        default <B> Generator<B> bind(F<A, Generator<B>> f) {
            return rand -> f.f(gen(rand)).gen(rand);
        }
    }

    static <A> Generator<A> oneIn(Set<A> list) {
        return rand -> {
            int n = rand.nextInt(list.size());
            return list.toList().drop(n).head();
        };
    }

    static <A> Generator<A> always(A value) {
        return rand -> value;
    }

    static <A> Generator<A> orWeight(int weight1, Generator<A> gen1, int weight2, Generator<A> gen2) {
        if (weight1 == 0)
            return gen2;
        else if (weight2 == 0)
            return gen1;
        else
            return rand -> {
                boolean choose1 = rand.nextDouble() < ((double) weight1) / (weight1 + weight2);
                return choose1 ? gen1.gen(rand) : gen2.gen(rand);
            };
    }

    static String para =
      "Spicy jalapeno bacon ipsum dolor amet leberkas t-bone salami " +
        "tri-tip ham hock.Chicken short ribs drumstick burgdoggen ribeye, " +
        "pork belly beef ribs filet mignon ball tip strip steak.Kevin alcatra turducken, sausage cow " +
        "burgdoggen andouille shank shankle shoulder flank jowl.Turducken meatloaf salami swine " +
        "prosciutto chuck corned beef frankfurter short ribs landjaeger, pastrami picanha.Prosciutto tongue pig " +
        "tenderloin, burgdoggen chuck spare ribs.Tri-tip landjaeger leberkas meatloaf porchetta t-bone corned beef pork " +
        "chop andouille pancetta tongue venison cupim pork belly.Ribeye t-bone tongue bresaola tail sirloin corned beef " +
        "fatback flank tenderloin tri-tip.Ground round meatloaf boudin kielbasa beef, prosciutto shoulder strip steak kevin." +
        "Chicken frankfurter ground round short ribs, bacon tri-tip swine burgdoggen andouille brisket." +
        "Tail tri-tip spare ribs, fatback shankle beef ribs capicola tenderloin." +
        "Cupim tenderloin bresaola beef ribs, pancetta meatball short ribs boudin chicken kielbasa spare ribs swine tri-tip pork belly brisket." +
        "Alcatra meatloaf pig pork belly jowl tenderloin." +
        "Ribeye corned beef jerky picanha boudin tri-tip ball tip." +
        "Shank sirloin bacon tenderloin biltong beef bresaola.";

    static Set<String> sentences =
      Set.arraySet(Ord.stringOrd, para.split("\\."));

    static Generator<String> randSentence =
      oneIn(sentences);
}
