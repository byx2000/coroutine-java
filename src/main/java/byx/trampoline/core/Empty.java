package byx.trampoline.core;

public class Empty<T> implements Trampoline<T> {
    public static final Empty<?> INSTANCE = new Empty<>();

    private Empty() {}
}
