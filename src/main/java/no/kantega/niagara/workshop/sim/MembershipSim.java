package no.kantega.niagara.workshop.sim;

import fj.*;
import fj.data.*;
import no.kantega.niagara.workshop.server.Progress;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static no.kantega.niagara.workshop.sim.Generator.*;
import static no.kantega.niagara.workshop.sim.Sim.*;
import static no.kantega.niagara.workshop.sim.SimOutput.*;

public class MembershipSim {


    static final AtomicLong personCounter = new AtomicLong();
    static final AtomicLong groupCounter  = new AtomicLong();


    public final Set<Person>     persons;
    public final Set<Group>      groups;
    public final Set<Membership> memberships;

    public MembershipSim(
        Set<Person> persons,
        Set<Group> groups,
        Set<Membership> memberships) {

        this.persons = persons;
        this.groups = groups;
        this.memberships = memberships;
    }

    public static MembershipSim newSimulation(Random random) {
        int         numOfPersons = random.nextInt(10) + 10;
        Set<Person> persons      = Set.iterableSet(personOrd, Stream.range(0, numOfPersons).map(i -> new Person("u" + personCounter.incrementAndGet())));
        Set<Group>  groups       = Set.empty(groupOrd);
        return new MembershipSim(persons, groups, Set.empty(membershipOrd));
    }


    public P2<MembershipSim, List<SimOutput>> next(Random rand) {

        Generator<P2<MembershipSim, List<SimOutput>>> gen =

            oneIn(persons).bind(person -> {

                Set<Group> isMemberOf = membershipsFor(memberships, person);

                Generator<P2<MembershipSim, List<SimOutput>>> g =
                    orWeight(
                        1, join(person, isMemberOf),
                        isMemberOf.size(), orWeight(1, leave(person, isMemberOf), 9, write(person)));

                return g;
            });

        return gen.gen(rand);
    }


    Generator<P2<MembershipSim, List<SimOutput>>> join(Person p, Set<Group> memberOf) {
        return rand -> {

            Set<Group> available =
                groups.minus(memberOf);

            boolean create = orWeight(groups.size(), always(false), 5, always(true)).gen(rand);

            if (create || available.isEmpty()) {
                Group           group         = new Group("g" + groupCounter.incrementAndGet());
                Set<Group>      newGroups     = groups.insert(group);
                Set<Membership> newMembership = memberships.insert(new Membership(p, group));
                return P.p(
                    new MembershipSim(persons, newGroups, newMembership),
                    List.list(new Create(p, group), new Join(p, group))
                );
            } else {
                Group           group         = oneIn(available).gen(rand);
                Set<Membership> newMembership = memberships.insert(new Membership(p, group));
                return P.p(
                    new MembershipSim(persons, groups, newMembership),
                    List.list(new Join(p, group))
                );
            }
        };
    }

    Generator<P2<MembershipSim, List<SimOutput>>> leave(Person p, Set<Group> memberOf) {
        return oneIn(memberOf).map(group -> {
            Set<Membership> newMembership = memberships.delete(new Membership(p, group));
            return P.p(
                new MembershipSim(persons, groups, newMembership),
                List.list(new Leave(p, group))
            );
        });
    }

    Generator<P2<MembershipSim, List<SimOutput>>> write(Person person) {
        return
            oneIn(membershipsFor(memberships, person))
                .bind(group -> oneIn(membersOf(memberships, group)).bind(m -> randSentence.map(s -> (SimOutput) new Post(person, group, s, List.nil()))
                    .or(randSentence.bind(s -> writeMention(group, person, s, m)).or(randSentence.bind(s -> oneIn(persons.minus(membersOf(memberships, group))).bind(p -> writeNoMention(group, person, s, p)))))
                    .map(out -> P.p(MembershipSim.this, List.single(out)))));
    }

    Generator<SimOutput> writeMention(Group group, Person author, String sentence, Person person) {
        return rand -> {
            Seq<String> splitted = Seq.arraySeq(sentence.split(" "));
            Seq<String> inserted = splitted.insert(rand.nextInt(splitted.length()), "@" + person.id);
            String      text     = inserted.tail().foldLeft((sum, word) -> sum + " " + word, inserted.head());
            return new Post(author, group, text, List.single(person));
        };
    }

    Generator<SimOutput> writeNoMention(Group group, Person author, String sentence, Person person) {
        return rand -> {
            Seq<String> splitted = Seq.arraySeq(sentence.split(" "));
            Seq<String> inserted = splitted.insert(rand.nextInt(splitted.length()), "@" + person.id);
            String      text     = inserted.tail().foldLeft((sum, word) -> sum + " " + word, inserted.head());
            return new Post(author, group, text, List.nil());
        };
    }


}
