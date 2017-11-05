package no.kantega.niagara.workshop.server;

import fj.Equal;
import fj.Ord;

public class SessionId {

    public static Ord<SessionId> sessionIdOrd =
      Ord.stringOrd.contramap(sessionId -> sessionId.value);

    public static Equal<SessionId> eq =
      sessionIdOrd.equal();

    public final String value;

    public SessionId(String value) {
        this.value = value;
    }

    public static SessionId sessId(String value) {
        return new SessionId(value);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SessionId{");
        sb.append("value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
