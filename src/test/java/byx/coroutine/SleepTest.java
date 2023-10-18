package byx.coroutine;

import byx.coroutine.core.Coroutine;
import byx.coroutine.dispatcher.Dispatcher;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static byx.coroutine.core.Thunks.exec;
import static byx.coroutine.dispatcher.Sleeper.sleep;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SleepTest {
    @Test
    public void testSleep() {
        Coroutine coroutine = exec(() -> System.out.println("begin"))
            .then(sleep(1, TimeUnit.SECONDS))
            .then(() -> System.out.println("end"))
            .toCoroutine();

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.addTask(coroutine);

        long begin = System.currentTimeMillis();
        dispatcher.run();
        long end = System.currentTimeMillis();

        assertTrue((end - begin) / 1000.0 >= 1.0);
    }
}
