package no.kantega.niagara.server;

import io.vertx.core.http.ServerWebSocket;

public class IncomingWsMessage {

    public final ServerWebSocket serverWebSocket;
    public final String          connectionId;
    public final String          message;

    public IncomingWsMessage(ServerWebSocket serverWebSocket, String connectionId, String message) {
        this.serverWebSocket = serverWebSocket;
        this.connectionId = connectionId;
        this.message = message;
    }
}
