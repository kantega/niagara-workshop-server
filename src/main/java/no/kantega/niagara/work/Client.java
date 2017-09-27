package no.kantega.niagara.work;

import fj.F;
import fj.Unit;
import fj.function.Effect1;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.WebSocket;
import org.kantega.kson.codec.JsonEncoder;
import org.kantega.kson.json.JsonObject;
import org.kantega.kson.json.JsonValue;
import org.kantega.kson.json.JsonValues;
import org.kantega.kson.parser.JsonParser;
import org.kantega.kson.parser.JsonWriter;
import org.kantega.niagara.Attempt;
import org.kantega.niagara.Task;

public class Client {

    static final HttpClient httpClient =
      Vertx.vertx().createHttpClient();

    public static Task<HttpClientResponse> get(int port, String server, String uri) {
        return Task.async(cb ->
          httpClient.get(port, server, uri).putHeader("x-client","vertx").handler(response -> cb.f(Attempt.value(response))).end());
    }

    public static Task<HttpClientResponse> post(int port, String server, String uri, String body) {
        return Task.async(cb ->
          httpClient.post(port, server, uri, response -> cb.f(Attempt.value(response))).end(body));
    }

    public static Task<Buffer> handleBody(HttpClientResponse resp) {
        return Task.async(cb -> resp.bodyHandler(buff -> cb.f(Attempt.value(buff))));
    }

    public static Task<WebSocket> openWebsocket(int port, String server, String uri) {
        return Task.async(cb ->
          httpClient.websocket(port, server, uri, ws -> cb.f(Attempt.value(ws))));
    }

    public static F<WebSocket, Task<WebSocket>> listen(Effect1<JsonValue> listener) {
        return ws ->
          Task.call(() ->
            ws.handler(buffer -> listener.f(JsonParser.parse(buffer.toString()).fold(JsonValues::jString, s -> s))));
    }

    public static <A> F<WebSocket, Task<WebSocket>> writeCmd(String path, A cmd, JsonEncoder<A> encoder) {
        JsonObject msg =
          JsonObject.empty
            .withField("path", JsonValues.jString(path))
            .withField("cmd", encoder.encode(cmd));

        return ws -> Task.call(() -> ws.write(Buffer.buffer(JsonWriter.writePretty(msg))));
    }

    public static Task<Unit> close(WebSocket ws) {
        return Task.runnableTask(ws::close);
    }
}
