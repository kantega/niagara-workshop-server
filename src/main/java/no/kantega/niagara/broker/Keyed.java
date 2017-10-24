package no.kantega.niagara.broker;

public class Keyed<A> {

    public final String key;
    public final A value;

    public Keyed(String key, A value) {
        this.key = key;
        this.value = value;
    }
}
