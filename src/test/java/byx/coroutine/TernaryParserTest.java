package byx.coroutine;

import byx.coroutine.core.Thunk;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static byx.coroutine.core.Thunk.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TernaryParserTest {
    @Test
    public void testTernaryParser() {
        assertEquals('3', eval1("F?1:T?3:1", new AtomicInteger()));
        assertEquals('2', eval1("T?2:3", new AtomicInteger()));
        assertEquals('F', eval1("T?T?F:5:3", new AtomicInteger()));
        assertEquals('1', eval1("T?T?T?T?1:1:1:1:1", new AtomicInteger()));
        assertEquals('6', eval1("F?F?7:T?8:A:6", new AtomicInteger()));
        assertEquals('F', eval1("T?F?2:F?T?F?F?F?F?2:T?T?8:F?F?F?F?T?T?0:F?F?F?5:T?T?F?F?T?F?T:F?3:F?T?F:F?9:F?T?T?F?T?T?9:T?T:T?F?T?T?T?F?3:T?T?F?F?F?F?5:F?1:F?2:T?T?F?3:F?T?T:F?T?1:F?2:T?T?T?T?F?T?F?T?F?8:F?F:T?T?2:T?F?F?9:T?F?0:T?T?T?4:F?F?7:T?F?T?5:F?0:F?1:T?T?F?T?T?3:T?0:F?2:T?5:T?T:F?F?F?T?8:F?T?6:F?F?T?7:T?F:T?F:T?T?T?F?F?7:F?F?F?3:T?F:F?F:T?F:F?F?F?T?F?F?T:F?T:T?T?T?F?9:F?1:T?T:T?9:F?F:T?T?7:T?8:F?F?6:F?F?8:F?T?1:F?1:T?F?7:T?F?8:F?7:F?F?0:F?T?F?3:F?T:T?7:F?6:F?T:F?6:F?T?2:F?F?F:F?2:F?T?F?2:T?F?T?F?F?T?2:T?T?F?2:F?4:T?F:T?9:T?F?T?T?T?T?T?T?4:A:2:7:0:3:9:3:1:8:4:3:7:8:1:4:F:T:6:0:3:4:6:3:0:T:2:F:7:5:1:2:2:F:5:8:4:7:5:2:F:2:F:5:6:4:6:4:4:3:3:5:2:3:T:F:6:0:1:0:0:3:F:2:0:3:5:4:6:5:6:8:3:T:0:8:5:5:9:T:4:3:F:8:0:2:9:1:0:0:0:8:T:T:4:5:F:8:F:7:T:8:4:2:5:F:7:F:1", new AtomicInteger()));

        assertEquals('3', eval2("F?1:T?3:1", new AtomicInteger()).run());
        assertEquals('2', eval2("T?2:3", new AtomicInteger()).run());
        assertEquals('F', eval2("T?T?F:5:3", new AtomicInteger()).run());
        assertEquals('1', eval2("T?T?T?T?1:1:1:1:1", new AtomicInteger()).run());
        assertEquals('6', eval2("F?F?7:T?8:A:6", new AtomicInteger()).run());
        assertEquals('F', eval2("T?F?2:F?T?F?F?F?F?2:T?T?8:F?F?F?F?T?T?0:F?F?F?5:T?T?F?F?T?F?T:F?3:F?T?F:F?9:F?T?T?F?T?T?9:T?T:T?F?T?T?T?F?3:T?T?F?F?F?F?5:F?1:F?2:T?T?F?3:F?T?T:F?T?1:F?2:T?T?T?T?F?T?F?T?F?8:F?F:T?T?2:T?F?F?9:T?F?0:T?T?T?4:F?F?7:T?F?T?5:F?0:F?1:T?T?F?T?T?3:T?0:F?2:T?5:T?T:F?F?F?T?8:F?T?6:F?F?T?7:T?F:T?F:T?T?T?F?F?7:F?F?F?3:T?F:F?F:T?F:F?F?F?T?F?F?T:F?T:T?T?T?F?9:F?1:T?T:T?9:F?F:T?T?7:T?8:F?F?6:F?F?8:F?T?1:F?1:T?F?7:T?F?8:F?7:F?F?0:F?T?F?3:F?T:T?7:F?6:F?T:F?6:F?T?2:F?F?F:F?2:F?T?F?2:T?F?T?F?F?T?2:T?T?F?2:F?4:T?F:T?9:T?F?T?T?T?T?T?T?4:A:2:7:0:3:9:3:1:8:4:3:7:8:1:4:F:T:6:0:3:4:6:3:0:T:2:F:7:5:1:2:2:F:5:8:4:7:5:2:F:2:F:5:6:4:6:4:4:3:3:5:2:3:T:F:6:0:1:0:0:3:F:2:0:3:5:4:6:5:6:8:3:T:0:8:5:5:9:T:4:3:F:8:0:2:9:1:0:0:0:8:T:T:4:5:F:8:F:7:T:8:4:2:5:F:7:F:1", new AtomicInteger()).run());


        String expr = "T?".repeat(100000) + "1" + ":1".repeat(100000);
        assertThrows(StackOverflowError.class, () -> eval1(expr, new AtomicInteger()));
        assertEquals('1', eval2(expr, new AtomicInteger()).run());
    }

    /**
     * expr = 'T'
     *      | 'F'
     *      | [0-9]
     *      | [TF] '?' expr ':' expr
     */
    private Character eval1(String expr, AtomicInteger index) {
        int i = index.getAndIncrement();
        char c = expr.charAt(i);
        if (c == 'T' || c == 'F') {
            if (i + 1 < expr.length() && expr.charAt(i + 1) == '?') {
                index.incrementAndGet();
                Character v1 = eval1(expr, index);
                index.incrementAndGet();
                Character v2 = eval1(expr, index);
                return c == 'T' ? v1 : v2;
            } else {
                return c;
            }
        } else {
            return c;
        }
    }

    private Thunk<Character> eval2(String expr, AtomicInteger index) {
        return exec(() -> {
            int i = index.getAndIncrement();
            char c = expr.charAt(i);
            if (c == 'T' || c == 'F') {
                if (i + 1 < expr.length() && expr.charAt(i + 1) == '?') {
                    return exec(index::incrementAndGet)
                        .then(() -> eval2(expr, index))
                        .flatMap(v1 -> exec(index::incrementAndGet)
                            .then(() -> eval2(expr, index))
                            .map(v2 -> c == 'T' ? v1 : v2));
                } else {
                    return value(c);
                }
            } else {
                return value(c);
            }
        });
    }
}
