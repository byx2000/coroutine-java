package byx.trampoline.dispatcher;

import byx.trampoline.core.Coroutine;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static byx.trampoline.core.Trampolines.exec;
import static byx.trampoline.core.Trampolines.value;
import static byx.trampoline.dispatcher.SystemCall.*;
import static org.junit.jupiter.api.Assertions.*;

public class DispatcherTest {
    @Test
    public void testAwait() {
        List<String> output = new ArrayList<>();

        Coroutine co1 = exec(() -> output.add("co1 begin"))
            .then(exec(() -> output.add("co1")).pause().repeat(2))
            .then(() -> output.add("co1 end"))
            .toCoroutine();
        Coroutine co2 = exec(() -> output.add("co2 begin"))
            .then(exec(() -> output.add("co2")).pause().repeat(3))
            .then(() -> output.add("co2 end"))
            .then(value(123))
            .toCoroutine();
        Coroutine co3 = exec(() -> output.add("co3 begin"))
            .then(await(co1))
            .then(r -> output.add("co1 return " + r))
            .then(await(co2, Integer.class))
            .then(r -> output.add("co2 return " + r))
            .then(exec(() -> output.add("co3")).pause().repeat(4))
            .then(() -> output.add("co3 end"))
            .toCoroutine();

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.addTask(co3);
        dispatcher.run();

        assertEquals(List.of(
            "co3 begin",
            "co1 begin",
            "co1",
            "co1",
            "co1 end",
            "co1 return null",
            "co2 begin",
            "co2",
            "co2",
            "co2",
            "co2 end",
            "co2 return 123",
            "co3",
            "co3",
            "co3",
            "co3",
            "co3 end"
        ), output);
    }

    @Test
    public void testCreateTask() {
        List<String> output = new ArrayList<>();

        Coroutine co1 = exec(() -> output.add("co1 begin"))
            .then(exec(() -> output.add("co1")).pause().repeat(3))
            .then(() -> output.add("co1 end"))
            .toCoroutine();
        Coroutine co2 = exec(() -> output.add("co2 begin"))
            .then(exec(() -> output.add("co2")).pause().repeat(4))
            .then(() -> output.add("co2 end"))
            .toCoroutine();
        Coroutine co3 = exec(() -> output.add("co3 begin"))
            .then(createTask(co1))
            .then(t1 -> {
                assertNotNull(t1);
                output.add("co1 created");
            })
            .then(createTask(co2))
            .then(t2 -> {
                assertNotNull(t2);
                output.add("co2 created");
            })
            .then(exec(() -> output.add("co3")).pause().repeat(6))
            .then(() -> output.add("co3 end"))
            .toCoroutine();

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.addTask(co3);
        dispatcher.run();

        assertEquals(List.of(
            "co3 begin",
            "co1 begin",
            "co1",
            "co1 created",
            "co1",
            "co2 begin",
            "co2",
            "co2 created",
            "co3",
            "co1",
            "co2",
            "co3",
            "co1 end",
            "co2",
            "co3",
            "co2",
            "co3",
            "co2 end",
            "co3",
            "co3",
            "co3 end"
        ), output);
    }

    @Test
    public void testWait1() {
        List<String> output = new ArrayList<>();

        Coroutine co1 = exec(() -> output.add("co1 begin"))
            .then(exec(() -> output.add("co1")).pause().repeat(3))
            .then(() -> output.add("co1 end"))
            .then(value("hello"))
            .toCoroutine();
        Coroutine co2 = exec(() -> output.add("co2 begin"))
            .then(exec(() -> output.add("co2")).pause().repeat(4))
            .then(() -> output.add("co2 end"))
            .then(value("hi"))
            .toCoroutine();
        Coroutine co3 = exec(() -> output.add("co3 begin"))
            .then(createTask(co1))
            .flatMap(t1 -> createTask(co2)
                .flatMap(t2 -> SystemCall.wait(t1, String.class)
                    .then(r1 -> output.add("co1 return " + r1))
                    .then(SystemCall.wait(t2))
                    .then(r2 -> output.add("co2 return " + r2))))
            .then(exec(() -> output.add("co3")).pause().repeat(6))
            .then(() -> output.add("co3 end"))
            .toCoroutine();

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.addTask(co3);
        dispatcher.run();

        assertEquals(List.of(
            "co3 begin",
            "co1 begin",
            "co1",
            "co1",
            "co2 begin",
            "co2",
            "co1",
            "co2",
            "co1 end",
            "co2",
            "co1 return hello",
            "co2",
            "co2 end",
            "co2 return hi",
            "co3",
            "co3",
            "co3",
            "co3",
            "co3",
            "co3",
            "co3 end"
        ), output);
    }

    @Test
    public void testWait2() {
        List<String> output = new ArrayList<>();

        Coroutine co1 = exec(() -> output.add("co1 begin"))
            .then(exec(() -> output.add("co1")).pause().repeat(4))
            .then(() -> output.add("co1 end"))
            .then(value("hello"))
            .toCoroutine();
        Coroutine co2 = exec(() -> output.add("co2 begin"))
            .then(exec(() -> output.add("co2")).pause().repeat(1))
            .then(() -> output.add("co2 end"))
            .then(value("hi"))
            .toCoroutine();
        Coroutine co3 = exec(() -> output.add("co3 begin"))
            .then(createTask(co1))
            .flatMap(t1 -> createTask(co2)
                .flatMap(t2 -> SystemCall.wait(t1, String.class)
                    .then(r1 -> output.add("co1 return " + r1))
                    .then(SystemCall.wait(t2))
                    .then(r2 -> output.add("co2 return " + r2))))
            .then(exec(() -> output.add("co3")).pause().repeat(6))
            .then(() -> output.add("co3 end"))
            .toCoroutine();

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.addTask(co3);
        dispatcher.run();

        assertEquals(List.of(
            "co3 begin",
            "co1 begin",
            "co1",
            "co1",
            "co2 begin",
            "co2",
            "co1",
            "co2 end",
            "co1",
            "co1 end",
            "co1 return hello",
            "co2 return hi",
            "co3",
            "co3",
            "co3",
            "co3",
            "co3",
            "co3",
            "co3 end"
        ), output);
    }

    @Test
    public void testWIthContinuation1() {
        List<String> output = new ArrayList<>();

        Coroutine co = exec(() -> output.add("co begin"))
            .then(withContinuation(c -> c.resume("hello"), String.class))
            .then(r -> output.add("result from resume is " + r))
            .then(() -> output.add("co end"))
            .toCoroutine();

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.addTask(co);
        dispatcher.run();

        assertEquals(List.of(
            "co begin",
            "result from resume is hello",
            "co end"
        ), output);
    }

    @Test
    public void testWIthContinuation2() {
        List<String> output = new ArrayList<>();
        long[] var = new long[]{0, 0};

        Coroutine co = exec(() -> {
            output.add("co begin");
            var[0] = System.currentTimeMillis();
        })
            .then(withContinuation(c -> new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                c.resume(123);
            }).start(), Integer.class))
            .then(r -> {
                output.add("result from resume is " + r);
                var[1] = System.currentTimeMillis();
            })
            .then(() -> output.add("co end"))
            .toCoroutine();

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.addTask(co);
        dispatcher.run();

        assertEquals(List.of(
            "co begin",
            "result from resume is 123",
            "co end"
        ), output);
        assertTrue(var[1] - var[0] >= 1000);
    }

    @Test
    public void testWIthContinuation3() {
        List<String> output = new ArrayList<>();

        Coroutine co1 = exec(() -> output.add("co1 begin"))
            .then(withContinuation(c -> new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                c.resume(123);
            }).start(), Integer.class))
            .then(r -> output.add("result from resume is " + r))
            .then(() -> output.add("co1 end"))
            .toCoroutine();
        Coroutine co2 = exec(() -> output.add("co2 begin"))
            .then(exec(() -> output.add("co2")).pause().repeat(5))
            .then(() -> output.add("co2 end"))
            .toCoroutine();

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.addTask(co1);
        dispatcher.addTask(co2);
        dispatcher.run();

        assertEquals(List.of(
            "co1 begin",
            "co2 begin",
            "co2",
            "co2",
            "co2",
            "co2",
            "co2",
            "co2 end",
            "result from resume is 123",
            "co1 end"
        ), output);
    }
}
