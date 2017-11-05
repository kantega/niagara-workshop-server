package no.kantega.niagara.workshop;

public class SubscribeTo {

    public final boolean replay ;
    public final String topic;

    public SubscribeTo(boolean replay, String topic) {
        this.replay = replay;
        this.topic = topic;
    }


    public static SubscribeTo subscribeTo(String topic){
        return new SubscribeTo(false,topic);
    }

    public static SubscribeTo replayAndSubscribeTo(String topic){
        return new SubscribeTo(true,topic);
    }
}
