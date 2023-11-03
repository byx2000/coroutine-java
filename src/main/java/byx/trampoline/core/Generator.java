package byx.trampoline.core;

import byx.trampoline.exception.EndOfCoroutineException;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 生成器
 */
public class Generator<T> implements Iterable<T>, Iterator<T> {
    private final Coroutine coroutine;
    private T peek;

    public Generator(Coroutine coroutine) {
        this.coroutine = coroutine;
    }

    @Override
    public Iterator<T> iterator() {
        return this;
    }

    /**
     * 是否有下一个元素
     */
    @Override
    public boolean hasNext() {
        if (peek != null) {
            return true;
        }
        try {
            peek = coroutine.run();
        } catch (EndOfCoroutineException e) {
            return false;
        }

        return true;
    }

    /**
     * 下一个元素
     */
    @Override
    public T next() {
        if (peek != null) {
            T ret = peek;
            peek = null;
            return ret;
        }
        return coroutine.run();
    }

    /**
     * 转换成Stream
     */
    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
