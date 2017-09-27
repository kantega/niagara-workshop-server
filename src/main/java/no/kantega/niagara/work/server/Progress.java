package no.kantega.niagara.work.server;

import fj.F;
import fj.P;
import fj.P2;
import fj.data.List;

public interface Progress {

    <T> T fold(F<Emit, T> onEmit, F<Await, T> onAwait, F<End, T> onDone);


    Progress then(Progress next);

    default boolean isDone() {
        return fold(
          e -> false,
          a -> false,
          d -> true
        );
    }

    default P2<List<ProgressMsg>, Progress> advance(String msg) {
        return fold(
          emit -> emit.next.advance(msg).map1(list -> list.cons(emit.messages)),
          await -> await.apply(msg).untilNextAwait(),
          end -> P.p(List.nil(), end)
        );
    }

    default P2<List<ProgressMsg>, Progress> untilNextAwait() {
        return fold(
          emit -> emit.next.untilNextAwait().map1(list -> list.cons(emit.messages)),
          await -> P.p(List.nil(), await),
          end -> P.p(List.nil(), end)
        );
    }

    default Progress repeat(int count) {
        return count > 1 ?
          fold(
            emit -> new Emit(emit.messages, repeat(count - 1)),
            await -> new Await(msg -> await.handler.f(msg).repeat(count - 1)),
            end -> end
          ) :
          this;
    }

    static Progress emit(String msg) {
        return emit(ProgressMsg.msg(msg));
    }

    static Progress emit(ProgressMsg msg) {
        return new Emit(msg, done());
    }

    static Progress await(F<String, Progress> func) {
        return new Await(func);
    }

    static Progress expect(F<String, Boolean> expect, F<String, Progress> onSuccess) {
        return await(msg -> expect.f(msg) ? onSuccess.f(msg) : expect(expect, onSuccess));
    }

    static Progress done() {
        return new End("Done");
    }

    static Progress fail(Exception e) {
        return new End(e.getCause().getClass().getSimpleName() + " " + e.getMessage());
    }


    class Emit implements Progress {
        public final ProgressMsg messages;
        public final Progress    next;

        public Emit(ProgressMsg messages, Progress next) {
            this.messages = messages;
            this.next = next;
        }

        @Override
        public <T> T fold(F<Emit, T> onEmit, F<Await, T> onAwait, F<End, T> onDone) {
            return onEmit.f(this);
        }

        @Override
        public Progress then(Progress other) {
            return new Emit(messages, next.then(other));
        }
    }

    class Await implements Progress {

        public final F<String, Progress> handler;

        public Await(F<String, Progress> handler) {
            this.handler = handler;
        }

        Progress apply(String incoming) {
            return handler.f(incoming);
        }

        @Override
        public <T> T fold(F<Emit, T> onEmit, F<Await, T> onAwait, F<End, T> onDone) {
            return onAwait.f(this);
        }

        @Override
        public Progress then(Progress next) {
            return new Await(msg -> handler.f(msg).then(next));
        }
    }

    class End implements Progress {
        public final String reason;

        public End(String reason) {
            this.reason = reason;
        }

        @Override
        public <T> T fold(F<Emit, T> onEmit, F<Await, T> onAwait, F<End, T> onDone) {
            return onDone.f(this);
        }

        @Override
        public Progress then(Progress next) {
            return fold(
              emit -> emit,
              await -> await,
              end -> end
            );
        }
    }

}
