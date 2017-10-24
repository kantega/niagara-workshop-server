package no.kantega.niagara.broker;

public class ConsumerRecord {

    public final String    id;
    public final TopicName topic;
    public final long      offset;
    public final String    msg;

    public ConsumerRecord(String id, long offset, TopicName topic, String msg) {
        this.id = id;
        this.topic = topic;
        this.offset = offset;
        this.msg = msg;
    }
}
