package no.kantega.niagara.exchange;

import fj.Unit;
import fj.data.List;
import no.kantega.niagara.stream.Source;
import no.kantega.niagara.stream.SourceListener;
import no.kantega.niagara.stream.Task;

import java.util.concurrent.CopyOnWriteArrayList;

public class Topic<A> {

    private final CopyOnWriteArrayList<SourceListener<A>> listeners =
      new CopyOnWriteArrayList<>();


    /**
     * @return
     */
    public Source<A> subscribe() {
        return (closer,handler) -> {
            listeners.add(handler);
            return
              closer.bind(stop->Task.runnableTask(()->listeners.remove(handler)).execute().map(u->Source.stopped()));
        };
    }

    public Task<Unit> publish(A a) {
        return
          Task.sequence(List.iterableList(listeners).map(listener->listener.handle(a))).toUnit();
    }

    public boolean isEmpty(){
        return listeners.isEmpty();
    }
}
