package byx.trampoline.core;

public record Value<T>(T value) implements Trampoline<T> {}
