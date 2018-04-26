package no.kantega.niagara.stream;


public interface Stream<A,B> {

    Source<B> apply(Source<A> input);

}
