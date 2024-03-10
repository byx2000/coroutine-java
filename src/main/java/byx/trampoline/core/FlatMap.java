package byx.trampoline.core;

import java.util.function.Function;

public record FlatMap<T, U>(Trampoline<T> head, Function<T, Trampoline<U>> mapper) implements Trampoline<U> {}
