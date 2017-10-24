package no.kantega.niagara.broker;

public class ProducerRecord {

    public final TopicName topic;
    public final String    msg;

    public ProducerRecord(TopicName topic, String msg) {
        this.topic = topic;
        this.msg = msg;
    }
}
