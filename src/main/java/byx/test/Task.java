package byx.test;

public class Task {
    private static long nextId = 1;

    private final Coroutine coroutine;
    private final long tid;
    private Object sendVal;

    public Task(Coroutine coroutine) {
        this.coroutine = coroutine;
        this.tid = nextId++;
    }

    public long getTid() {
        return tid;
    }

    public void setSendVal(Object sendVal) {
        this.sendVal = sendVal;
    }

    public Object run() {
        return coroutine.run(sendVal);
    }
}
