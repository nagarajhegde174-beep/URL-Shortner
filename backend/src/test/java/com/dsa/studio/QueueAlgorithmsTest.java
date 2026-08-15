package com.dsa.studio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;


class QueueAlgorithmsTest {

    // ── Enqueue / Dequeue Tests ───────────────────────────────────────────────

    @Test
    void testEnqueueDequeue_normalCase() {
        Queue<Integer> q = new LinkedList<>();
        q.offer(10); q.offer(20); q.offer(30);
        assertEquals(10, q.poll());
        assertEquals(20, q.poll());
        assertEquals(30, q.poll());
        assertTrue(q.isEmpty());
    }

    @Test
    void testEnqueue_singleElement() {
        Queue<Integer> q = new LinkedList<>();
        q.offer(42);
        assertEquals(42, q.peek());
        assertEquals(1, q.size());
    }

    @Test
    void testDequeue_emptyQueue_returnsNull() {
        Queue<Integer> q = new LinkedList<>();
        assertNull(q.poll());
    }

    @Test
    void testPeek_emptyQueue_returnsNull() {
        Queue<Integer> q = new LinkedList<>();
        assertNull(q.peek());
    }

    @Test
    void testEnqueue_duplicateValues() {
        Queue<Integer> q = new LinkedList<>();
        q.offer(5); q.offer(5); q.offer(5);
        assertEquals(3, q.size());
        assertEquals(5, q.poll());
        assertEquals(5, q.poll());
    }

    // ── Circular Queue Tests ───────────────────────────────────────────────────

    static class CircularQueue {
        int[] arr;
        int front = 0, rear = 0, size = 0;

        CircularQueue(int cap) { arr = new int[cap]; }

        boolean enqueue(int x) {
            if (size == arr.length) return false;
            arr[rear] = x;
            rear = (rear + 1) % arr.length;
            size++;
            return true;
        }

        int dequeue() {
            if (size == 0) return -1;
            int val = arr[front];
            front = (front + 1) % arr.length;
            size--;
            return val;
        }

        int peek() { return size == 0 ? -1 : arr[front]; }
        boolean isFull() { return size == arr.length; }
        boolean isEmpty() { return size == 0; }
    }

    @Test
    void testCircularQueue_normalCase() {
        CircularQueue cq = new CircularQueue(4);
        cq.enqueue(10); cq.enqueue(20); cq.enqueue(30);
        assertEquals(10, cq.dequeue());
        cq.enqueue(40); cq.enqueue(50);
        // After enqueue 10,20,30 (size=3), dequeue (size=2), enqueue 40,50 (size=4)
        // Size should be 4
        assertEquals(4, cq.size);
    }

    @Test
    void testCircularQueue_fullCapacity() {
        CircularQueue cq = new CircularQueue(3);
        assertTrue(cq.enqueue(1));
        assertTrue(cq.enqueue(2));
        assertTrue(cq.enqueue(3));
        assertTrue(cq.isFull());
        assertFalse(cq.enqueue(4)); // overflow
    }

    @Test
    void testCircularQueue_emptyDequeue() {
        CircularQueue cq = new CircularQueue(3);
        assertEquals(-1, cq.dequeue());
    }

    @Test
    void testCircularQueue_wrapAround() {
        CircularQueue cq = new CircularQueue(3);
        cq.enqueue(1); cq.enqueue(2); cq.enqueue(3);
        cq.dequeue(); cq.dequeue();
        // rear now wraps
        cq.enqueue(4); cq.enqueue(5);
        assertEquals(3, cq.dequeue());
        assertEquals(4, cq.dequeue());
        assertEquals(5, cq.dequeue());
        assertTrue(cq.isEmpty());
    }

    @Test
    void testCircularQueue_singleElement() {
        CircularQueue cq = new CircularQueue(1);
        assertTrue(cq.enqueue(99));
        assertTrue(cq.isFull());
        assertEquals(99, cq.dequeue());
        assertTrue(cq.isEmpty());
    }

    // ── Priority Queue Tests ────────────────────────────────────────────────────

    @Test
    void testPriorityQueue_minHeap_extractOrder() {
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        pq.offer(40); pq.offer(10); pq.offer(30); pq.offer(20);
        List<Integer> order = new ArrayList<>();
        while (!pq.isEmpty()) order.add(pq.poll());
        assertEquals(List.of(10, 20, 30, 40), order);
    }

    @Test
    void testPriorityQueue_peek_returnsMin() {
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        pq.offer(7); pq.offer(3); pq.offer(5);
        assertEquals(3, pq.peek());
    }

    @Test
    void testPriorityQueue_singleElement() {
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        pq.offer(1);
        assertEquals(1, pq.poll());
        assertTrue(pq.isEmpty());
    }

    @Test
    void testPriorityQueue_emptyPoll_returnsNull() {
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        assertNull(pq.poll());
    }

    @Test
    void testPriorityQueue_duplicateValues() {
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        pq.offer(5); pq.offer(5); pq.offer(5);
        assertEquals(3, pq.size());
        assertEquals(5, pq.poll());
        assertEquals(5, pq.poll());
    }

    // ── Sliding Window Maximum Tests ────────────────────────────────────────────

    private int[] slidingWindowMax(int[] arr, int k) {
        int n = arr.length;
        if (n == 0 || k == 0) return new int[0];
        int[] result = new int[n - k + 1];
        Deque<Integer> deque = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            while (!deque.isEmpty() && deque.peekFirst() < i - k + 1) deque.pollFirst();
            while (!deque.isEmpty() && arr[deque.peekLast()] < arr[i]) deque.pollLast();
            deque.offerLast(i);
            if (i >= k - 1) result[i - k + 1] = arr[deque.peekFirst()];
        }
        return result;
    }

    @Test
    void testSlidingWindowMax_normalCase() {
        int[] result = slidingWindowMax(new int[]{1, 3, -1, -3, 5, 3, 6, 7}, 3);
        assertArrayEquals(new int[]{3, 3, 5, 5, 6, 7}, result);
    }

    @Test
    void testSlidingWindowMax_windowEqualsArraySize() {
        int[] result = slidingWindowMax(new int[]{1, 2, 3}, 3);
        assertArrayEquals(new int[]{3}, result);
    }

    @Test
    void testSlidingWindowMax_windowSizeOne() {
        int[] result = slidingWindowMax(new int[]{4, 2, 8, 5}, 1);
        assertArrayEquals(new int[]{4, 2, 8, 5}, result);
    }

    @Test
    void testSlidingWindowMax_emptyArray() {
        int[] result = slidingWindowMax(new int[]{}, 3);
        assertEquals(0, result.length);
    }

    @Test
    void testSlidingWindowMax_allSameValues() {
        int[] result = slidingWindowMax(new int[]{5, 5, 5, 5}, 2);
        assertArrayEquals(new int[]{5, 5, 5}, result);
    }

    @Test
    void testSlidingWindowMax_descendingArray() {
        int[] result = slidingWindowMax(new int[]{5, 4, 3, 2, 1}, 2);
        assertArrayEquals(new int[]{5, 4, 3, 2}, result);
    }

    // ── Array-based Queue Overflow/Underflow ────────────────────────────────────

    @Test
    void testArrayQueue_overflow_doesNotAdvanceRear() {
        int[] arr = new int[3];
        int front = 0, rear = -1, size = 0;
        // Fill to capacity
        if (size < arr.length) { arr[++rear] = 1; size++; }
        if (size < arr.length) { arr[++rear] = 2; size++; }
        if (size < arr.length) { arr[++rear] = 3; size++; }
        int rearBefore = rear;
        // Attempt overflow
        if (size < arr.length) { arr[++rear] = 4; size++; } // should not execute
        assertEquals(rearBefore, rear, "Rear should not advance past capacity");
        assertEquals(3, size);
    }

    @Test
    void testArrayQueue_underflow_returnsSentinel() {
        int[] arr = new int[3];
        int front = 0, rear = -1, size = 0;
        int result = (size > 0) ? arr[front++] : -1;
        assertEquals(-1, result);
    }
}
