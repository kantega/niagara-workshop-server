package no.kantega.niagara.work.server;

public class ProgressOutput {

    public final String message;

    public ProgressOutput(String message) {
        this.message = message;
    }

    static ProgressOutput msg(String text) {
        return new ProgressOutput(text);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProgressMsg{");
        sb.append("message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
