package no.kantega.niagara.workshop.server;

import fj.F;
import fj.P;
import fj.P2;
import fj.data.List;

public interface Progress {

    <T> T fold(F<Emit, T> onEmit, F<Receive, T> onAwait, F<End, T> onDone);


    Progress then(Progress next);

    default boolean isDone() {
        return fold(
          e -> false,
          a -> false,
          d -> true
        );
    }


    default P2< Progress,List<ProgressOutput>> advance(ProgressInput msg) {
        return fold(
          emit -> emit.next.advance(msg).map2(list -> list.cons(emit.messages)),
          await -> await.apply(msg).begin(),
          end -> P.p(end,List.nil())
        );
    }

    default P2< Progress,List<ProgressOutput>> begin() {
        return fold(
          emit -> emit.next.begin().map2(list -> list.cons(emit.messages)),
          await -> P.p(await,List.nil()),
          end -> P.p(end,List.nil())
        );
    }

    default Progress repeat(int count) {
        return count > 1 ?
          fold(
            emit -> new Emit(emit.messages, repeat(count - 1)),
            await -> new Receive(msg -> await.handler.f(msg).repeat(count - 1)),
            end -> end
          ) :
          this;
    }

    static Progress emit(String msg) {
        return emit(ProgressOutput.msg(msg));
    }

    static Progress emit(ProgressOutput msg) {
        return new Emit(msg, done());
    }

    static Progress await(F<ProgressInput, Progress> func) {
        return new Receive(func);
    }

    static Progress expect(
      F<ProgressInput, Boolean> expect,
      F<ProgressInput, Progress> onSuccess) {
        return expect(expect, msg -> expect(expect, onSuccess), onSuccess);
    }

    static Progress expectWithFailMsg(
      F<ProgressInput, Boolean> expect,
      F<ProgressInput, String> failmsg,
      F<ProgressInput, Progress> onSuccess) {
        return expect(expect, msg -> emit(failmsg.f(msg)).then(expectWithFailMsg(expect, failmsg, onSuccess)), onSuccess);
    }

    static Progress expect(
      F<ProgressInput, Boolean> expect,
      F<ProgressInput, Progress> onFail,
      F<ProgressInput, Progress> onSuccess) {
        return await(msg -> expect.f(msg) ? onSuccess.f(msg) : onFail.f(msg));
    }

    static Progress done() {
        return new End("Done");
    }

    static Progress fail(Exception e) {
        return new End(e.getCause().getClass().getSimpleName() + " " + e.getMessage());
    }


    class Emit implements Progress {
        public final ProgressOutput messages;
        public final Progress       next;

        public Emit(ProgressOutput messages, Progress next) {
            this.messages = messages;
            this.next = next;
        }

        @Override
        public <T> T fold(F<Emit, T> onEmit, F<Receive, T> onAwait, F<End, T> onDone) {
            return onEmit.f(this);
        }

        @Override
        public Progress then(Progress other) {
            return new Emit(messages, next.then(other));
        }
    }

    class Receive implements Progress {

        public final F<ProgressInput, Progress> handler;

        public Receive(F<ProgressInput, Progress> handler) {
            this.handler = handler;
        }

        Progress apply(ProgressInput incoming) {
            return handler.f(incoming);
        }

        @Override
        public <T> T fold(F<Emit, T> onEmit, F<Receive, T> onAwait, F<End, T> onDone) {
            return onAwait.f(this);
        }

        @Override
        public Progress then(Progress next) {
            return new Receive(msg -> handler.f(msg).then(next));
        }
    }

    class End implements Progress {

        public final String reason;

        public End(String reason) {
            this.reason = reason;
        }

        @Override
        public <T> T fold(F<Emit, T> onEmit, F<Receive, T> onAwait, F<End, T> onDone) {
            return onDone.f(this);
        }

        @Override
        public Progress then(Progress next) {
            return next;
        }
    }

}
