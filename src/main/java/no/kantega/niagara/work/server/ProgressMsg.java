package no.kantega.niagara.work.server;

public class ProgressMsg {

    public final String message;

    public ProgressMsg(String message) {
        this.message = message;
    }

    static ProgressMsg msg(String text) {
        return new ProgressMsg(text);
    }



}
