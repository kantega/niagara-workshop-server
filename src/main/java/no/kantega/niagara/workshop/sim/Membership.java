package no.kantega.niagara.workshop.sim;

public class Membership {
    public final Person person;
    public final Group  group;

    Membership(Person person, Group group) {
        this.person = person;
        this.group = group;
    }
}