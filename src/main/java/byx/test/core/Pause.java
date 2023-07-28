package byx.test.core;

public class Pause<T> implements Thunk<T> {
    private final Object value;

    public Pause(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
