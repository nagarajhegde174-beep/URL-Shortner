// Queue Operations & Algorithm Templates
// Used by QueuePage.jsx for algorithm selection, code editor, and comparison features.

export const QUEUE_OPERATIONS = {
  queueArray: {
    name: 'Queue using Array',
    className: 'QueueArray',
    category: 'Fundamentals',
    explanation:
      'A queue implemented with a fixed-size array. It uses a front pointer to track dequeue actions and a rear pointer to track enqueue actions. Underflow occurs when size is 0, and overflow when size reaches capacity.',
    timeComplexity: 'O(1) enqueue/dequeue',
    spaceComplexity: 'O(N)',
    javaCode: `public class QueueArray {
    static int[] arr = new int[5];
    static int front = 0;
    static int rear = -1;
    static int size = 0;

    static void enqueue(int x) {
        if (size < arr.length) {
            arr[++rear] = x;
            size++;
            System.out.println("Enqueued: " + x);
        } else {
            System.out.println("Queue Overflow");
        }
    }

    static int dequeue() {
        if (size > 0) {
            int val = arr[front++];
            size--;
            System.out.println("Dequeued: " + val);
            return val;
        }
        System.out.println("Queue Underflow");
        return -1;
    }

    static int peek() {
        return size > 0 ? arr[front] : -1;
    }

    public static void main(String[] args) {
        enqueue(10);
        enqueue(20);
        enqueue(30);
        System.out.println("Peek: " + peek());
        System.out.println("Dequeue: " + dequeue());
        System.out.println("Dequeue: " + dequeue());
        System.out.println("Peek: " + peek());
    }
}`,
    interviewQuestions: [
      'What are the limitations of a linear array-based queue?',
      'Why do we need a circular queue rather than resetting pointers on array boundaries?',
      'What is the time complexity of shifting elements vs shifting pointers in a linear queue?',
    ],
    commonMistakes: [
      'Forgetting to track size or bounds, causing IndexOutOfBoundsException.',
      'Assuming rear pointer resets to -1 automatically when elements are popped.',
    ],
    optimizedVersion: '// A circular queue solves the pointer shifting issue by reusing indexes.',
  },

  javaQueue: {
    name: 'Java Queue (LinkedList)',
    className: 'JavaQueue',
    category: 'Fundamentals',
    explanation:
      'In Java, Queue is an interface. java.util.LinkedList is commonly used as a queue since it implements the Queue interface. Standard methods include offer() to enqueue, poll() to dequeue, and peek() to view front.',
    timeComplexity: 'O(1) all operations',
    spaceComplexity: 'O(N)',
    javaCode: `import java.util.Queue;
import java.util.LinkedList;

public class JavaQueue {
    public static void main(String[] args) {
        Queue<Integer> queue = new LinkedList<>();

        queue.offer(10);
        queue.offer(20);
        queue.offer(30);

        System.out.println("Peek: " + queue.peek());
        System.out.println("Poll: " + queue.poll());
        System.out.println("Size: " + queue.size());
        System.out.println("Empty: " + queue.isEmpty());
    }
}`,
    interviewQuestions: [
      'What is the difference between offer() and add() in Java Queue?',
      'What is the difference between poll() and remove() in Java Queue?',
      'Why is LinkedList preferred over ArrayList for implementing java.util.Queue?',
    ],
    commonMistakes: [
      'Using add() instead of offer() — add() throws exception on capacity restrictions, whereas offer() returns false.',
      'Using poll() on empty queue returns null, while remove() throws NoSuchElementException.',
    ],
    optimizedVersion: '// ArrayDeque is generally more memory-efficient than LinkedList for queues.',
  },

  circularQueue: {
    name: 'Circular Queue',
    className: 'CircularQueue',
    category: 'Fundamentals',
    explanation:
      'A circular queue wraps pointers back to index 0 using the modulus operator when they reach the end of the backing array. This avoids wasting memory slots that become free after dequeue operations.',
    timeComplexity: 'O(1) all operations',
    spaceComplexity: 'O(N)',
    javaCode: `public class CircularQueue {
    int[] arr;
    int front = 0;
    int rear = 0;
    int size = 0;

    CircularQueue(int capacity) {
        arr = new int[capacity];
    }

    void enqueue(int x) {
        if (size == arr.length) {
            System.out.println("Queue full");
            return;
        }
        arr[rear] = x;
        rear = (rear + 1) % arr.length;
        size++;
    }

    int dequeue() {
        if (size == 0) {
            return -1;
        }
        int val = arr[front];
        front = (front + 1) % arr.length;
        size--;
        return val;
    }

    public static void main(String[] args) {
        CircularQueue cq = new CircularQueue(4);
        cq.enqueue(10);
        cq.enqueue(20);
        cq.enqueue(30);
        System.out.println("Dequeued: " + cq.dequeue());
        cq.enqueue(40);
        cq.enqueue(50);
        System.out.println("Dequeued: " + cq.dequeue());
        System.out.println("Current Size: " + cq.size);
    }
}`,
    interviewQuestions: [
      'How does modulus help in circular wrapping?',
      'How do you distinguish between queue empty and queue full conditions when not using a size variable?',
      'Write the condition for circular rear increment.',
    ],
    commonMistakes: [
      'Forgetting to compute modulo when incrementing front/rear pointers.',
      'Allowing front and rear to overlap and overwrite values without size checking.',
    ],
    optimizedVersion: '// Checking full/empty without size: empty when front == rear, full when (rear + 1) % capacity == front.',
  },

  priorityQueue: {
    name: 'Priority Queue',
    className: 'PriorityQueueDemo',
    category: 'Algorithms',
    explanation:
      'A java.util.PriorityQueue processes elements in priority order (min-heap by default in Java). Elements are retrieved in sorted order regardless of their enqueue order.',
    timeComplexity: 'O(log N) offer/poll, O(1) peek',
    spaceComplexity: 'O(N)',
    javaCode: `import java.util.PriorityQueue;

public class PriorityQueueDemo {
    public static void main(String[] args) {
        PriorityQueue<Integer> pq = new PriorityQueue<>();

        pq.offer(40);
        pq.offer(10);
        pq.offer(30);
        pq.offer(20);

        System.out.println("Min/Peek: " + pq.peek());
        while (!pq.isEmpty()) {
            System.out.println(pq.poll());
        }
    }
}`,
    interviewQuestions: [
      'How does a Priority Queue differ from a standard Queue?',
      'How does PriorityQueue implement ordering for custom objects?',
      'What is the internal data structure of PriorityQueue?',
    ],
    commonMistakes: [
      'Assuming iteration over PriorityQueue prints elements in sorted order. Only polling retrieves sorted values.',
      'Adding non-comparable items to a PriorityQueue without providing a Comparator.',
    ],
    optimizedVersion: '// Provide custom Comparator to change min-heap to max-heap (e.g. Collections.reverseOrder()).',
  },

  generateBinary: {
    name: 'Generate Binary Numbers',
    className: 'PracticeBinaryNums',
    category: 'Algorithms',
    explanation:
      'Generate binary numbers from 1 to N using a queue. Generate the next numbers by appending "0" and "1" to the dequeued front element.',
    timeComplexity: 'O(N)',
    spaceComplexity: 'O(N)',
    javaCode: `import java.util.LinkedList;
import java.util.Queue;

public class PracticeBinaryNums {
    public static void main(String[] args) {
        int n = 5;
        Queue<String> queue = new LinkedList<>();
        queue.offer("1");

        for (int i = 0; i < n; i++) {
            String front = queue.poll();
            System.out.println(front);
            queue.offer(front + "0");
            queue.offer(front + "1");
        }
    }
}`,
    interviewQuestions: [
      'Why is Queue suitable for generating binary numbers in order?',
      'Can we generate base-3 or base-N numbers similarly?',
    ],
    commonMistakes: [
      'Starting with "0" as the root element which produces numbers with leading zeros.',
      'Forgetting to print the popped front value.',
    ],
    optimizedVersion: '// O(N) since each binary string is generated and processed once.',
  },
};

export const COMPARISON_TEMPLATES = {
  queueArray: {
    label: 'Array Queue',
    className: 'QueueArray',
    time: 'O(1)',
    space: 'O(N) fixed',
    javaCode: `public class QueueArray {
    static int[] arr = new int[5];
    static int front = 0, rear = -1, size = 0;
    static void enqueue(int x) { if (size < arr.length) { arr[++rear] = x; size++; } }
    static int dequeue() { return size > 0 ? arr[front++] : -1; }
    public static void main(String[] args) {
        enqueue(10);
        enqueue(20);
        dequeue();
        System.out.println("Size: " + size);
    }
}`,
  },

  javaQueue: {
    label: 'Java Queue',
    className: 'JavaQueueComp',
    time: 'O(1)',
    space: 'O(N) dynamic',
    javaCode: `import java.util.LinkedList;
import java.util.Queue;
public class JavaQueueComp {
    public static void main(String[] args) {
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(10);
        queue.offer(20);
        queue.poll();
        System.out.println("Size: " + queue.size());
    }
}`,
  },
};

export const QUEUE_QUIZ_QUESTIONS = [
  {
    type: 'MCQ',
    question: 'Which of the following describes the behavior of a Queue?',
    options: ['LIFO (Last In First Out)', 'FIFO (First In First Out)', 'LILO (Last In Last Out)', 'FILO (First In Last Out)'],
    answer: 'FIFO (First In First Out)',
    explanation: 'Queues maintain insertion order: the first element added is the first element removed (FIFO).',
  },
  {
    type: 'MCQ',
    question: 'What is the time complexity of enqueuing and dequeuing in an array-based Circular Queue?',
    options: ['O(1)', 'O(log N)', 'O(N)', 'O(N log N)'],
    answer: 'O(1)',
    explanation: 'Circular queues maintain front and rear indices, allowing constant-time O(1) additions and removals without element shifting.',
  },
  {
    type: 'PREDICTION',
    question: 'What is the default ordering of elements in Java java.util.PriorityQueue?',
    options: [
      'FIFO order of insertion',
      'LIFO order of insertion',
      'Natural ordering / min-heap (lowest element first)',
      'Random order',
    ],
    answer: 'Natural ordering / min-heap (lowest element first)',
    explanation: 'PriorityQueue default constructor orders elements according to their natural ordering (minimum element is at head of queue).',
  },
];
