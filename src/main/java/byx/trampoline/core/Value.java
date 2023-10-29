package byx.trampoline.core;

public class Value<T> implements Trampoline<T> {
    private final T value;

    public Value(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
