package byx.test.core;

import byx.test.exception.EndOfCoroutineException;

public interface Coroutine {
    default <T> T run() throws EndOfCoroutineException {
        return run(null);
    }

    <T> T run(Object value) throws EndOfCoroutineException;
}
