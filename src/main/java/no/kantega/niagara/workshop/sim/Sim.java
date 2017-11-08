package no.kantega.niagara.workshop.sim;

import fj.Ord;
import fj.P;
import fj.data.Set;

public class Sim {

    static final Ord<Group>      groupOrd      = Ord.stringOrd.contramap(g -> g.id);
    static final Ord<Person>     personOrd     = Ord.stringOrd.contramap(p -> p.id);
    static final Ord<Membership> membershipOrd = Ord.p2Ord(personOrd, groupOrd).contramap(m -> P.p(m.person, m.group));

    public static Set<Person> membersOf(Set<Membership> memberships,Group group) {
        return memberships.filter(m -> m.group.id.equals(group.id)).map(personOrd, m -> m.person);
    }

    public static Set<Group> membershipsFor(Set<Membership> memberships,Person person) {
        return memberships.filter(m -> m.person.id.equals(person.id)).map(groupOrd, m -> m.group);
    }
}
