package byx.test.core;

public class Value<T> implements Thunk<T> {
    private final T value;

    public Value(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
