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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConsumerRecord{");
        sb.append("id='").append(id).append('\'');
        sb.append(", topic=").append(topic);
        sb.append(", offset=").append(offset);
        sb.append(", msg='").append(msg).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
