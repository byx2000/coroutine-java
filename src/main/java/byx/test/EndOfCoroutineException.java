package byx.test;

/**
 * 协程运行结束后，继续调用run方法则抛出此异常
 */
public class EndOfCoroutineException extends RuntimeException {
    private final Object returnValue;

    public EndOfCoroutineException(Object returnValue) {
        super("coroutine is end, return value is " + returnValue);
        this.returnValue = returnValue;
    }

    public Object getReturnValue() {
        return returnValue;
    }
}
