package no.kantega.niagara.stream;

import fj.Unit;

public interface SourceListener<A>  {

    Task<Unit> handle(A a);

}
