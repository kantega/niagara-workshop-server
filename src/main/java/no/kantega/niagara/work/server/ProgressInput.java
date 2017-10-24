package no.kantega.niagara.work.server;


public class ProgressInput {

    public final SessionId sessionId;
    public final String    message;

    public ProgressInput(SessionId sessionId, String message) {
        this.sessionId = sessionId;
        this.message = message;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IncomingMessage{");
        sb.append("sessionId=").append(sessionId);
        sb.append(", message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

