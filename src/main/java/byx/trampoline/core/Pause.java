package byx.trampoline.core;

public class Pause<T> implements Trampoline<T> {
    private final Object value;

    public Pause(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
