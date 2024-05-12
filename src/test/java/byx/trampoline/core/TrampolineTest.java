package byx.trampoline.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static byx.trampoline.core.Trampolines.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TrampolineTest {
    @Test
    public void testEmpty() {
        assertNull(empty().run());
    }

    @Test
    public void testValue() {
        assertEquals(123, value(123).run());
        assertEquals("hello", value(() -> "hello").run());
    }

    @Test
    public void testExec1() {
        AtomicInteger i = new AtomicInteger(123);
        Trampoline<?> trampoline = exec(() -> i.set(456));
        assertEquals(123, i.get());
        trampoline.run();
        assertEquals(456, i.get());
    }

    @Test
    public void testExec2() {
        AtomicInteger i = new AtomicInteger(123);
        Trampoline<String> trampoline = exec(() -> {
            i.set(456);
            return value("hello");
        });
        assertEquals(123, i.get());
        assertEquals("hello", trampoline.run());
        assertEquals(456, i.get());
    }

    @Test
    public void testLoop() {
        List<Integer> output = new ArrayList<>();
        AtomicInteger i = new AtomicInteger(0);
        Trampoline<?> trampoline = loop(
            () -> i.get() <= 5,
            () -> {
                output.add(i.get());
                i.getAndIncrement();
            }
        );
        trampoline.run();
        assertEquals(List.of(0, 1, 2, 3, 4, 5), output);
    }

    @Test
    public void testThen1() {
        List<String> output = new ArrayList<>();
        exec(() -> output.add("hello"))
            .then(exec(() -> output.add("hi")))
            .run();
        assertEquals(List.of("hello", "hi"), output);
    }

    @Test
    public void testThen2() {
        AtomicInteger i = new AtomicInteger();
        Trampoline<String> trampoline = value("hello")
            .then(() -> i.set(123));
        assertEquals("hello", trampoline.run());
        assertEquals(123, i.get());
    }

    @Test
    public void testThen3() {
        List<String> output = new ArrayList<>();
        exec(() -> output.add("hello"))
            .then(() -> exec(() -> output.add("hi")))
            .run();
        assertEquals(List.of("hello", "hi"), output);
    }

    @Test
    public void testThen4() {
        AtomicInteger i = new AtomicInteger();
        Trampoline<String> trampoline = value("hello")
            .then(n -> i.set(456));
        assertEquals("hello", trampoline.run());
        assertEquals(456, i.get());
    }

    @Test
    public void testThenValue1() {
        Trampoline<String> trampoline = empty().value("hello");
        assertEquals("hello", trampoline.run());
    }

    @Test
    public void testThenValue2() {
        AtomicInteger i = new AtomicInteger(123);
        Trampoline<String> trampoline = empty()
            .value(() -> {
                i.set(456);
                return "hello";
            });
        assertEquals(123, i.get());
        assertEquals("hello", trampoline.run());
        assertEquals(456, i.get());
    }

    @Test
    public void testThenLoop() {
        List<Integer> output = new ArrayList<>();
        AtomicInteger i = new AtomicInteger(0);
        Trampoline<?> trampoline = empty().loop(
            () -> i.get() <= 5,
            () -> {
                output.add(i.get());
                i.getAndIncrement();
            }
        );
        trampoline.run();
        assertEquals(List.of(0, 1, 2, 3, 4, 5), output);
    }

    @Test
    public void testRepeat() {
        List<Integer> output = new ArrayList<>();
        exec(() -> output.add(123)).repeat(5).run();
        assertEquals(List.of(123, 123, 123, 123, 123), output);
    }
}
