package no.kantega.niagara.work;

import fj.F;
import fj.P;
import fj.P2;
import fj.Unit;
import fj.data.List;
import fj.data.Option;
import org.kantega.niagara.Task;

public class Util {

    public static Task<Unit> println(String line) {
        return Task.runnableTask(() -> System.out.println(line));
    }

    public static Task<String> printlnThrough(String line) {
        return println(line).map(u -> line);
    }

    public static <B, C> Matcher<C> caseOf(Class<B> tpe, F<B, C> handler) {
        return new Matcher<>(List.single(v -> tpe.isInstance(v) ? Option.some(handler.f(tpe.cast(v))) : Option.none()));
    }

    public static Option<P2<String, String>> splitBefore(String string, String split) {
        if (string.contains(split)) {
            String first = string.substring(0, string.indexOf(split));
            String last  = string.substring(string.indexOf(split));
            return Option.some(P.p(first, last));
        } else {
            return Option.none();
        }
    }

    public static Option<String> substringUntil(String origin, String until) {
        return indexOf(origin, until).map(index -> origin.substring(0, index));
    }

    public static Option<Integer> indexOf(String str, String test) {
        return
          Option
            .some(str.indexOf(test))
            .bind(index -> index == -1 ? Option.none() : Option.some(index));
    }

    public static class Matcher<C> {

        public final List<F<Object, Option<C>>> casters;

        public Matcher(List<F<Object, Option<C>>> casters) {
            this.casters = casters;
        }

        public <B> Matcher<C> caseOf(Class<B> tpe, F<B, C> handler) {
            return new Matcher<>(casters.cons(v -> tpe.isInstance(v) ? Option.some(handler.f(tpe.cast(v))) : Option.none()));
        }

        public Option<C> get(Object value) {
            return casters.reverse().foldLeft((sum, elem) -> sum.orElse(elem.f(value)), Option.none());
        }

    }
}
