package byx.trampoline.example.generator;

import byx.trampoline.core.Generator;
import byx.trampoline.core.Trampoline;
import org.junit.jupiter.api.Test;

import java.util.List;

import static byx.trampoline.core.Trampolines.loop;
import static byx.trampoline.core.Trampolines.pause;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NestedListGeneratorTest {
    @Test
    public void testNestedListGenerator() {
        List<?> list = List.of(1, List.of(2, 3), 4, 5, List.of(6, List.of(7, 8)), List.of(9));
        Generator<Integer> generator = nestedListGenerator(list);
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), generator.stream().toList());
    }

    private Generator<Integer> nestedListGenerator(List<?> list) {
        return nestedListTraverse(list).toGenerator();
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
