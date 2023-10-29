package byx.trampoline.core;

import java.util.function.Function;

public class FlatMap<T, U> implements Trampoline<U> {
    private final Trampoline<T> head;
    private final Function<T, Trampoline<U>> mapper;

    public FlatMap(Trampoline<T> head, Function<T, Trampoline<U>> mapper) {
        this.head = head;
        this.mapper = mapper;
    }

    public Trampoline<T> getHead() {
        return head;
    }

    public Function<T, Trampoline<U>> getMapper() {
        return mapper;
    }
}
