package no.kantega.niagara.server;

public class OutgoingWsMessage {
    public final String connectionId;
    public final String message;

    public OutgoingWsMessage(String connectionId, String message) {
        this.connectionId = connectionId;
        this.message = message;
    }
}
