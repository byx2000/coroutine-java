package byx.test.dispatcher;

import byx.test.core.Thunk;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import static byx.test.dispatcher.SystemCall.withContinuation;

public class Sleeper {
    private static class Item implements Delayed {
        private final long time;
        private final Runnable runnable;

        private Item(long time, TimeUnit unit, Runnable runnable) {
            this.time = System.currentTimeMillis() + (time > 0 ? unit.toMillis(time) : 0);
            this.runnable = runnable;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return time - System.currentTimeMillis();
        }

        @Override
        public int compareTo(Delayed o) {
            Item item = (Item) o;
            return (int) (this.time - item.time);
        }
    }

    private static final DelayQueue<Item> dq = new DelayQueue<>();
    private static final Thread bgThread = new Thread(() -> {
        while (true) {
            try {
                dq.take().runnable.run();
            } catch (InterruptedException e) {
                break;
            }
        }
    }, "thread-sleeper");

    static {
        bgThread.setDaemon(true);
        bgThread.start();
    }

    public static <T> Thunk<T> sleep(long time, TimeUnit unit) {
        return withContinuation(c -> dq.add(new Item(time, unit, c::resume)));
    }
}
