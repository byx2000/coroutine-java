package byx.trampoline.example.generator;

import byx.trampoline.core.Coroutine;
import byx.trampoline.core.Trampoline;
import byx.trampoline.exception.EndOfCoroutineException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static byx.trampoline.core.Trampolines.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NestedListGeneratorTest {
    @Test
    public void testNestedListGenerator() {
        List<?> list = List.of(1, List.of(2, 3), 4, 5, List.of(6, List.of(7, 8)), List.of(9));
        Coroutine generator = nestedListGenerator(list);
        assertEquals(1, (int) generator.run());
        assertEquals(2, (int) generator.run());
        assertEquals(3, (int) generator.run());
        assertEquals(4, (int) generator.run());
        assertEquals(5, (int) generator.run());
        assertEquals(6, (int) generator.run());
        assertEquals(7, (int) generator.run());
        assertEquals(8, (int) generator.run());
        assertEquals(9, (int) generator.run());
        assertThrows(EndOfCoroutineException.class, generator::run);
    }

    private Coroutine nestedListGenerator(List<?> list) {
        return nestedListTraverse(list).toCoroutine();
    }

    private Trampoline<Void> nestedListTraverse(List<?> list) {
        return loop(list, (i, e) -> {
            if (e instanceof List<?>) {
                return nestedListTraverse((List<?>) e);
            } else {
                return pause(e);
            }
        });
    }
}
