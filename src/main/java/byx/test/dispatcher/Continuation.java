package byx.test.dispatcher;

public interface Continuation<T> {
    void resume(T value);
}
