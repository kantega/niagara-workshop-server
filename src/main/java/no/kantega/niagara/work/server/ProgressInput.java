package no.kantega.niagara.work.server;


public class ProgressInput {

    public final String message;

    public ProgressInput(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IncomingMessage{");
        sb.append(", message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

