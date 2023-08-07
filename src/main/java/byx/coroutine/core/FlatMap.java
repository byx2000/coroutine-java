package byx.coroutine.core;

import java.util.function.Function;

public class FlatMap<T, U> implements Thunk<U> {
    private final Thunk<T> thunk;
    private final Function<T, Thunk<U>> mapper;

    public FlatMap(Thunk<T> thunk, Function<T, Thunk<U>> mapper) {
        this.thunk = thunk;
        this.mapper = mapper;
    }

    public Thunk<T> getThunk() {
        return thunk;
    }

    public Function<T, Thunk<U>> getMapper() {
        return mapper;
    }
}
