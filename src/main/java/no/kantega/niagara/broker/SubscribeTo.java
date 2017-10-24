package no.kantega.niagara.broker;

public class SubscribeTo {

    public final boolean replay ;
    public final String topic;

    public SubscribeTo(boolean replay, String topic) {
        this.replay = replay;
        this.topic = topic;
    }
}
