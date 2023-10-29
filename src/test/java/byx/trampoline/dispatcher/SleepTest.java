package byx.trampoline.dispatcher;

import byx.trampoline.core.Coroutine;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static byx.trampoline.core.Trampolines.exec;
import static byx.trampoline.dispatcher.Sleeper.sleep;
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
