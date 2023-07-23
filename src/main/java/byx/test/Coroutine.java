package byx.test;

public interface Coroutine<T> {
    default T run() throws EndOfCoroutineException {
        return run(null);
    }

    T run(Object value) throws EndOfCoroutineException;
}
