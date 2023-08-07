package byx.coroutine;

import byx.coroutine.core.Thunk;
import org.junit.jupiter.api.Test;

import java.util.List;

import static byx.coroutine.core.Thunk.*;
import static org.junit.jupiter.api.Assertions.*;

public class ListNodeTest {
    @Test
    public void testReverseList() {
        assertList(List.of(5, 4, 3, 2, 1), reverseList1(buildList(5)));
        assertList(List.of(5, 4, 3, 2, 1), reverseList2(buildList(5)).run());
    }

    @Test
    public void testStackOverflow() {
        assertThrows(StackOverflowError.class, () -> reverseList1(buildList(100000)));

        ListNode head = reverseList2(buildList(100000)).run();
        for (int i = 100000; i >= 1; i--) {
            assertEquals(i, head.val);
            head = head.next;
        }
    }

    private void assertList(List<Integer> expected, ListNode head) {
        ListNode p = head;
        for (Integer n : expected) {
            assertNotNull(p);
            assertEquals(n, p.val);
            p = p.next;
        }
        assertNull(p);
    }

    private ListNode buildList(int length) {
        ListNode head = new ListNode(1);
        ListNode p = head;
        for (int i = 2; i <= length; i++) {
            p.next = new ListNode(i);
            p = p.next;
        }
        return head;
    }

    private ListNode reverseList1(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }

        ListNode p = reverseList1(head.next);
        head.next.next = head;
        head.next = null;
        return p;
    }

    private Thunk<ListNode> reverseList2(ListNode head) {
        if (head == null || head.next == null) {
            return value(head);
        }

        return exec(() -> reverseList2(head.next))
            .map(p -> {
                head.next.next = head;
                head.next = null;
                return p;
            });
    }
}

class ListNode {
    public int val;
    public ListNode next;

    public ListNode(int val) {
        this.val = val;
    }
}
