package byx.trampoline.dispatcher;

import byx.trampoline.core.Coroutine;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static byx.trampoline.core.Trampolines.exec;
import static byx.trampoline.dispatcher.Sleeper.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SleepTest {
    @Test
    public void testSleep() {
        List<String> output = new ArrayList<>();
        Coroutine co1 = exec(() -> output.add("aaa"))
            .then(sleep(2, TimeUnit.SECONDS))
            .then(() -> output.add("bbb"))
            .toCoroutine();
        Coroutine co2 = sleep(1, TimeUnit.SECONDS)
            .then(() -> output.add("ccc"))
            .toCoroutine();

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.addTask(co1);
        dispatcher.addTask(co2);
        dispatcher.run();

        assertEquals(List.of("aaa", "ccc", "bbb"), output);
    }
}
