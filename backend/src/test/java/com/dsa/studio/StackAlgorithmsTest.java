package com.dsa.studio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Phase 7 — Stack Algorithm Tests
 * Tests core Stack algorithms and DSA logic used in the Stack learning module.
 * These are pure algorithmic tests (no Spring context needed).
 */
class StackAlgorithmsTest {

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Push elements onto the stack. */
    private void pushAll(Stack<Integer> st, int... values) {
        for (int v : values) st.push(v);
    }

    // ── Push / Pop Tests ─────────────────────────────────────────────────────

    @Test
    void testPushPop_normalCase() {
        Stack<Integer> st = new Stack<>();
        pushAll(st, 10, 20, 30);
        assertEquals(30, st.pop());
        assertEquals(20, st.pop());
        assertEquals(10, st.pop());
        assertTrue(st.isEmpty());
    }

    @Test
    void testPush_singleElement() {
        Stack<Integer> st = new Stack<>();
        st.push(42);
        assertEquals(42, st.peek());
        assertEquals(1, st.size());
    }

    @Test
    void testPop_emptyStack_throwsException() {
        Stack<Integer> st = new Stack<>();
        assertThrows(EmptyStackException.class, st::pop);
    }

    @Test
    void testPeek_emptyStack_throwsException() {
        Stack<Integer> st = new Stack<>();
        assertThrows(EmptyStackException.class, st::peek);
    }

    @Test
    void testPush_duplicateValues() {
        Stack<Integer> st = new Stack<>();
        pushAll(st, 5, 5, 5);
        assertEquals(3, st.size());
        assertEquals(5, st.pop());
        assertEquals(5, st.pop());
    }

    // ── Balanced Parentheses Tests ────────────────────────────────────────────

    private boolean isBalanced(String s) {
        Stack<Character> stack = new Stack<>();
        for (char c : s.toCharArray()) {
            if (c == '(' || c == '{' || c == '[') {
                stack.push(c);
            } else {
                if (stack.isEmpty()) return false;
                char top = stack.pop();
                if ((c == ')' && top != '(') || (c == '}' && top != '{') || (c == ']' && top != '[')) return false;
            }
        }
        return stack.isEmpty();
    }

    @Test
    void testBalancedParentheses_validMixed() {
        assertTrue(isBalanced("({[]})"));
    }

    @Test
    void testBalancedParentheses_validSimple() {
        assertTrue(isBalanced("()"));
        assertTrue(isBalanced("[]"));
        assertTrue(isBalanced("{}"));
    }

    @Test
    void testBalancedParentheses_invalidMismatch() {
        assertFalse(isBalanced("({)}"));
        assertFalse(isBalanced("[)"));
    }

    @Test
    void testBalancedParentheses_emptyString() {
        assertTrue(isBalanced(""));
    }

    @Test
    void testBalancedParentheses_onlyOpen() {
        assertFalse(isBalanced("((("));
    }

    @Test
    void testBalancedParentheses_singleChar() {
        assertFalse(isBalanced("("));
        assertFalse(isBalanced(")"));
    }

    // ── Next Greater Element Tests ─────────────────────────────────────────────

    private int[] nextGreaterElement(int[] arr) {
        int n = arr.length;
        int[] nge = new int[n];
        Arrays.fill(nge, -1);
        Stack<Integer> stack = new Stack<>();
        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && arr[stack.peek()] < arr[i]) {
                nge[stack.pop()] = arr[i];
            }
            stack.push(i);
        }
        return nge;
    }

    @Test
    void testNGE_normalCase() {
        int[] result = nextGreaterElement(new int[]{4, 5, 2, 10, 8});
        assertArrayEquals(new int[]{5, 10, 10, -1, -1}, result);
    }

    @Test
    void testNGE_descendingArray() {
        int[] result = nextGreaterElement(new int[]{5, 4, 3, 2, 1});
        assertArrayEquals(new int[]{-1, -1, -1, -1, -1}, result);
    }

    @Test
    void testNGE_ascendingArray() {
        int[] result = nextGreaterElement(new int[]{1, 2, 3, 4, 5});
        assertArrayEquals(new int[]{2, 3, 4, 5, -1}, result);
    }

    @Test
    void testNGE_singleElement() {
        int[] result = nextGreaterElement(new int[]{7});
        assertArrayEquals(new int[]{-1}, result);
    }

    @Test
    void testNGE_duplicateValues() {
        int[] result = nextGreaterElement(new int[]{3, 3, 3});
        assertArrayEquals(new int[]{-1, -1, -1}, result);
    }

    // ── Min Stack Tests ────────────────────────────────────────────────────────

    static class MinStack {
        private final Stack<Integer> stack = new Stack<>();
        private final Stack<Integer> minStack = new Stack<>();

        void push(int x) {
            stack.push(x);
            if (minStack.isEmpty() || x <= minStack.peek()) minStack.push(x);
        }

        int pop() {
            int val = stack.pop();
            if (val == minStack.peek()) minStack.pop();
            return val;
        }

        int getMin() {
            return minStack.peek();
        }

        boolean isEmpty() { return stack.isEmpty(); }
    }

    @Test
    void testMinStack_normalCase() {
        MinStack ms = new MinStack();
        ms.push(5); ms.push(3); ms.push(7); ms.push(1);
        assertEquals(1, ms.getMin());
        ms.pop(); // remove 1
        assertEquals(3, ms.getMin());
        ms.pop(); // remove 7
        assertEquals(3, ms.getMin());
    }

    @Test
    void testMinStack_singleElement() {
        MinStack ms = new MinStack();
        ms.push(42);
        assertEquals(42, ms.getMin());
        ms.pop();
        assertTrue(ms.isEmpty());
    }

    @Test
    void testMinStack_duplicateMinValues() {
        MinStack ms = new MinStack();
        ms.push(2); ms.push(2); ms.push(2);
        assertEquals(2, ms.getMin());
        ms.pop();
        assertEquals(2, ms.getMin()); // still 2
    }

    @Test
    void testMinStack_allSameValues() {
        MinStack ms = new MinStack();
        ms.push(5); ms.push(5); ms.push(5);
        assertEquals(5, ms.getMin());
    }

    @Test
    void testMinStack_increasing_then_decreasing() {
        MinStack ms = new MinStack();
        ms.push(1); ms.push(2); ms.push(3);
        assertEquals(1, ms.getMin());
        ms.pop(); ms.pop(); ms.pop();
        ms.push(10); ms.push(5); ms.push(8);
        assertEquals(5, ms.getMin());
    }

    // ── Array-backed Stack Overflow/Underflow Tests ────────────────────────────

    @Test
    void testArrayStack_overflow() {
        // capacity 3
        int[] arr = new int[3];
        int top = -1;
        top = (top < arr.length - 1) ? ++top : top; arr[top] = 1;
        top = (top < arr.length - 1) ? ++top : top; arr[top] = 2;
        top = (top < arr.length - 1) ? ++top : top; arr[top] = 3;
        // At capacity — push should not increase top further
        int before = top;
        int newTop = (top < arr.length - 1) ? top + 1 : top; // should be same
        assertEquals(before, newTop, "Top should not advance beyond capacity");
    }

    @Test
    void testArrayStack_underflow() {
        int[] arr = new int[3];
        int top = -1;
        // pop from empty
        int result = (top >= 0) ? arr[top--] : -1;
        assertEquals(-1, result, "Underflow should return sentinel -1");
    }
}
