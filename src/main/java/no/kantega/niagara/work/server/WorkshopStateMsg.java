package no.kantega.niagara.work.server;

public class WorkshopStateMsg {

    public final SessionId      sessionId;
    public final ProgressOutput progressMsg;

    public WorkshopStateMsg(SessionId sessionId, ProgressOutput progressMsg) {
        this.sessionId = sessionId;
        this.progressMsg = progressMsg;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WorkshopStateMsg{");
        sb.append("sessionId=").append(sessionId);
        sb.append(", progressMsg=").append(progressMsg);
        sb.append('}');
        return sb.toString();
    }
}
