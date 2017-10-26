package no.kantega.niagara.broker;

import fj.F;
import fj.Ord;
import fj.Unit;
import fj.data.List;
import fj.data.Set;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import org.kantega.kson.parser.JsonWriter;
import org.kantega.niagara.Sink;
import org.kantega.niagara.Task;

public class ClientSubscription implements Sink<ConsumerRecord> {

    public final F<String, Boolean> spec;
    public final ServerWebSocket          webSocket;

    public ClientSubscription(F<String, Boolean> spec, ServerWebSocket webSocket) {
        this.spec = spec;
        this.webSocket = webSocket;
    }

    public static ClientSubscription subscription(ServerWebSocket webSocket, List<String> topics) {
        Set<String> set = Set.iterableSet(Ord.stringOrd, topics);
        return new ClientSubscription(set::member, webSocket);
    }

    public static ClientSubscription firehose(ServerWebSocket webSocket) {
        return new ClientSubscription(str -> true, webSocket);
    }

    @Override
    public Task<Unit> consume(ConsumerRecord consumerRecord) {
        return
          spec.f(consumerRecord.topic.name) ?
            Task.tryRunnableTask(() -> webSocket.write(Buffer.buffer(JsonWriter.write(Client.consumerRecordCodec.encode(consumerRecord))))) :
            Task.noOp;
    }
}
