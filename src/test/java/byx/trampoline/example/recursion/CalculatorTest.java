package byx.trampoline.example.recursion;

import byx.trampoline.core.Trampoline;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static byx.trampoline.core.Trampolines.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CalculatorTest {
    @Test
    public void testCalculator() {
        assertEquals(2, Calculator1.eval("1+1"));
        assertEquals(14, Calculator1.eval("2*(3+4)"));
        assertEquals(45, Calculator1.eval("(2+3)*(4+5)"));
        assertEquals(23, Calculator1.eval("(1+(4+5+2)-3)+(6+8)"));
        assertEquals(-42, Calculator1.eval("((((1*10)-(3*8))*3)*1)"));
        assertEquals(-12, Calculator1.eval("(4+(((5+4)-(7-9))-(10+(9+8))))"));
        assertEquals(-12, Calculator1.eval("(2+6*3+5-(3*14/7+2)*5)+3"));
        assertEquals(-35, Calculator1.eval("5+3-4-(1+2-7+(10-1+3+5+(3-0+(8-(3+(8-(10-(6-10-8-7+(0+0+7)-10+5-3-2+(9+0+(7+(2-(2-(9)-2+5+4+2+(2+9+1+5+5-8-9-2-9+1+0)-(5-(9)-(0-(7+9)+(10+(6-4+6))+0-2+(10+7+(8+(7-(8-(3)+(2)+(10-6+10-(2)-7-(2)+(3+(8))+(1-3-8)+6-(4+1)+(6))+6-(1)-(10+(4)+(8)+(5+(0))+(3-(6))-(9)-(4)+(2))))))-1)))+(9+6)+(0))))+3-(1))+(7))))))))"));
        assertThrows(StackOverflowError.class, () -> Calculator1.eval("-".repeat(100000) + "1"));
        assertThrows(StackOverflowError.class, () -> Calculator1.eval("(".repeat(100000) + "1" + ")".repeat(100000)));

        assertEquals(2, Calculator2.eval("1+1"));
        assertEquals(14, Calculator2.eval("2*(3+4)"));
        assertEquals(45, Calculator2.eval("(2+3)*(4+5)"));
        assertEquals(23, Calculator2.eval("(1+(4+5+2)-3)+(6+8)"));
        assertEquals(-42, Calculator2.eval("((((1*10)-(3*8))*3)*1)"));
        assertEquals(-12, Calculator2.eval("(4+(((5+4)-(7-9))-(10+(9+8))))"));
        assertEquals(-12, Calculator2.eval("(2+6*3+5-(3*14/7+2)*5)+3"));
        assertEquals(-35, Calculator2.eval("5+3-4-(1+2-7+(10-1+3+5+(3-0+(8-(3+(8-(10-(6-10-8-7+(0+0+7)-10+5-3-2+(9+0+(7+(2-(2-(9)-2+5+4+2+(2+9+1+5+5-8-9-2-9+1+0)-(5-(9)-(0-(7+9)+(10+(6-4+6))+0-2+(10+7+(8+(7-(8-(3)+(2)+(10-6+10-(2)-7-(2)+(3+(8))+(1-3-8)+6-(4+1)+(6))+6-(1)-(10+(4)+(8)+(5+(0))+(3-(6))-(9)-(4)+(2))))))-1)))+(9+6)+(0))))+3-(1))+(7))))))))"));
        assertEquals(1, Calculator2.eval("-".repeat(100000) + "1"));
        assertEquals(1, Calculator2.eval("(".repeat(100000) + "1" + ")".repeat(100000)));
    }
}

class Calculator1 {
    public static int eval(String expr) {
        return evalExpr(expr, new AtomicInteger());
    }

    /**
     * expr = term (+|- term)*
     */
    private static int evalExpr(String expr, AtomicInteger index) {
        int res = evalTerm(expr, index);
        while (index.get() < expr.length() && (expr.charAt(index.get()) == '+' || expr.charAt(index.get()) == '-')) {
            int i = index.getAndIncrement();
            if (expr.charAt(i) == '+') {
                res += evalTerm(expr, index);
            } else {
                res -= evalTerm(expr, index);
            }
        }
        return res;
    }

    /**
     * term = fact (*|/ fact)*
     */
    private static int evalTerm(String expr, AtomicInteger index) {
        int res = evalFact(expr, index);
        while (index.get() < expr.length() && (expr.charAt(index.get()) == '*' || expr.charAt(index.get()) == '/')) {
            int i = index.getAndIncrement();
            if (expr.charAt(i) == '*') {
                res *= evalFact(expr, index);
            } else {
                res /= evalFact(expr, index);
            }
        }
        return res;
    }

    /**
     * fact = (expr)
     *      | -fact
     *      | num
     */
    private static int evalFact(String expr, AtomicInteger index) {
        if (expr.charAt(index.get()) == '(') {
            index.incrementAndGet();
            int res = evalExpr(expr, index);
            index.incrementAndGet();
            return res;
        } else if (expr.charAt(index.get()) == '-') {
            index.incrementAndGet();
            return -evalFact(expr, index);
        } else {
            return evalNum(expr, index);
        }
    }

    /**
     * num = [0-9]+
     */
    private static int evalNum(String expr, AtomicInteger index) {
        int res = 0;
        while (index.get() < expr.length() && Character.isDigit(expr.charAt(index.get()))) {
            res = res * 10 + (expr.charAt(index.get()) - '0');
            index.incrementAndGet();
        }
        return res;
    }
}

class Calculator2 {
    public static int eval(String expr) {
        return evalExpr(expr, new AtomicInteger()).run();
    }

    /**
     * expr = term (+|- term)*
     */
    private static Trampoline<Integer> evalExpr(String expr, AtomicInteger index) {
        int[] res = new int[]{0};
        return exec(() -> evalTerm(expr, index))
            .then(r -> res[0] = r)
            .loop(
                () -> index.get() < expr.length() && (expr.charAt(index.get()) == '+' || expr.charAt(index.get()) == '-'),
                () -> {
                    int i = index.getAndIncrement();
                    if (expr.charAt(i) == '+') {
                        return exec(() -> evalTerm(expr, index))
                            .then(r -> res[0] += r);
                    } else {
                        return exec(() -> evalTerm(expr, index))
                            .then(r -> res[0] -= r);
                    }
                }
            )
            .value(() -> res[0]);
    }

    /**
     * term = fact (*|/ fact)*
     */
    private static Trampoline<Integer> evalTerm(String expr, AtomicInteger index) {
        int[] res = new int[]{0};
        return exec(() -> evalFact(expr, index))
            .then(r -> res[0] = r)
            .loop(
                () -> index.get() < expr.length() && (expr.charAt(index.get()) == '*' || expr.charAt(index.get()) == '/'),
                () -> {
                    int i = index.getAndIncrement();
                    if (expr.charAt(i) == '*') {
                        return exec(() -> evalFact(expr, index))
                            .then(r -> res[0] *= r);
                    } else {
                        return exec(() -> evalFact(expr, index))
                            .then(r -> res[0] /= r);
                    }
                }
            )
            .value(() -> res[0]);
    }

    /**
     * fact = (expr)
     *      | -fact
     *      | num
     */
    private static Trampoline<Integer> evalFact(String expr, AtomicInteger index) {
        if (expr.charAt(index.get()) == '(') {
            return exec(index::incrementAndGet)
                .then(() -> evalExpr(expr, index))
                .then(index::incrementAndGet);
        } else if (expr.charAt(index.get()) == '-') {
            return exec(index::incrementAndGet)
                .then(() -> evalFact(expr, index))
                .map(res -> -res);
        } else {
            return value(() -> evalNum(expr, index));
        }
    }

    /**
     * num = [0-9]+
     */
    private static int evalNum(String expr, AtomicInteger index) {
        int res = 0;
        while (index.get() < expr.length() && Character.isDigit(expr.charAt(index.get()))) {
            res = res * 10 + (expr.charAt(index.get()) - '0');
            index.incrementAndGet();
        }
        return res;
    }
}
