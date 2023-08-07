package byx.test.dispatcher;

/**
 * 封装协程剩余的运行流程，可在任意时刻恢复协程运行
 */
public interface Continuation {
    /**
     * 恢复协程运行，并发送指定值value
     *
     * @param value value
     */
    void resume(Object value);

    /**
     * 恢复协程运行，默认发送null
     */
    default void resume() {
        resume(null);
    }
}
