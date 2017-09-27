package no.kantega.niagara.work.server;

public class IncMsg {

    public final String sessionId;
    public final String payload;

    public IncMsg(String sessionId, String payload) {
        this.sessionId = sessionId;
        this.payload = payload;
    }
}
