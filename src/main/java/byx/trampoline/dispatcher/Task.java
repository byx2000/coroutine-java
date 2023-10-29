package byx.trampoline.dispatcher;

import byx.trampoline.core.Coroutine;

/**
 * 任务
 */
public class Task {
    private static long nextId = 1;

    private final Coroutine coroutine;
    private final long tid;
    private Object sendVal;
    private Object retVal;

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

    public Object getRetVal() {
        return retVal;
    }

    public void setRetVal(Object retVal) {
        this.retVal = retVal;
    }

    public Object run() {
        return coroutine.run(sendVal);
    }
}
