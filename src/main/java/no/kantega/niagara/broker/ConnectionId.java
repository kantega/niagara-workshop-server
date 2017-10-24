package no.kantega.niagara.broker;

public class ConnectionId {

    public final String value;

    public ConnectionId(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConnectionId{");
        sb.append("value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
