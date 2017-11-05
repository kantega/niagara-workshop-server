package no.kantega.niagara.broker;

import fj.F;

public class ProducerRecord {

    public final TopicName topic;
    public final String    msg;

    public ProducerRecord(TopicName topic, String msg) {
        this.topic = topic;
        this.msg = msg;
    }

    public static ProducerRecord message(TopicName topicName, String msg) {
        return new ProducerRecord(topicName, msg);
    }

    public static F<String, ProducerRecord> toMessage(TopicName topicName) {
        return (msg) -> message(topicName, msg);
    }

    @Override
    public String toString() {
        return "ProducerRecord{" +
          "topic=" + topic +
          ", msg='" + msg + '\'' +
          '}';
    }
}
