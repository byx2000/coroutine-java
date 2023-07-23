package byx.test;

/**
 * 协程运行结束后，继续调用run方法则抛出此异常
 */
public class EndOfCoroutineException extends RuntimeException {
    public EndOfCoroutineException() {
        super("coroutine is end");
    }
}
