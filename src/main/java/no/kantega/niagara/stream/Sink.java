package no.kantega.niagara.stream;

import fj.Unit;

public interface Sink<A> {

    public Task<Unit> consume(A a);

}
