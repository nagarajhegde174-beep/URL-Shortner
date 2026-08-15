// Stack Operations & Algorithm Templates
// Used by StackPage.jsx for algorithm selection, code editor, and comparison features.

export const STACK_OPERATIONS = {
  // ── Fundamentals ────────────────────────────────────────────────────────
  stackArray: {
    name: 'Stack using Array',
    className: 'StackArray',
    category: 'Fundamentals',
    explanation:
      'A stack implemented with a fixed-size array. A top pointer tracks the index of the most recently pushed element. LIFO: the last element pushed is the first one popped.',
    timeComplexity: 'O(1) push/pop/peek',
    spaceComplexity: 'O(N)',
    javaCode: `public class StackArray {
    static int[] arr = new int[5];
    static int top = -1;

    static void push(int x) {
        if (top < arr.length - 1) {
            arr[++top] = x;
            System.out.println("Pushed: " + x);
        } else {
            System.out.println("Stack Overflow");
        }
    }

    static int pop() {
        if (top >= 0) {
            int val = arr[top--];
            System.out.println("Popped: " + val);
            return val;
        }
        System.out.println("Stack Underflow");
        return -1;
    }

    static int peek() {
        return top >= 0 ? arr[top] : -1;
    }

    public static void main(String[] args) {
        push(10);
        push(20);
        push(30);
        System.out.println("Peek: " + peek());
        pop();
        pop();
        System.out.println("Peek: " + peek());
    }
}`,
    interviewQuestions: [
      'What happens on stack overflow with an array implementation?',
      'Why is top initialized to -1 instead of 0?',
      'How would you resize an array-backed stack dynamically?',
    ],
    commonMistakes: [
      'Forgetting to check bounds before push (overflow) or pop (underflow).',
      'Off-by-one error: using `arr[top]` after increment vs before decrement.',
    ],
    optimizedVersion: '// For dynamic sizing, use java.util.ArrayList internally instead of a fixed array.',
  },

  javaStack: {
    name: 'Java Stack',
    className: 'JavaStack',
    category: 'Fundamentals',
    explanation:
      'java.util.Stack extends Vector and provides thread-safe push/pop/peek operations. In modern Java, ArrayDeque is preferred for non-thread-safe contexts due to better performance.',
    timeComplexity: 'O(1) amortized push/pop',
    spaceComplexity: 'O(N)',
    javaCode: `import java.util.Stack;

public class JavaStack {
    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<>();

        stack.push(10);
        stack.push(20);
        stack.push(30);

        System.out.println("Peek: " + stack.peek());
        System.out.println("Pop: " + stack.pop());
        System.out.println("Size: " + stack.size());
        System.out.println("Empty: " + stack.isEmpty());
        System.out.println("Search 10: " + stack.search(10));
    }
}`,
    interviewQuestions: [
      'Why is ArrayDeque preferred over java.util.Stack in modern Java?',
      'What does Stack.search() return for a missing element?',
    ],
    commonMistakes: [
      'Using java.util.Stack in performance-critical single-threaded code (it is synchronized).',
      'Expecting 0-based search result — Stack.search() returns 1-based distance from top.',
    ],
    optimizedVersion: '// Use Deque<Integer> stack = new ArrayDeque<>() for better performance.',
  },

  dequeStack: {
    name: 'Deque as Stack',
    className: 'DequeStack',
    category: 'Fundamentals',
    explanation:
      'ArrayDeque implements Deque and can act as both a stack and a queue. Use push()/pop()/peek() for stack behavior. This is the preferred approach in modern Java.',
    timeComplexity: 'O(1) all operations',
    spaceComplexity: 'O(N)',
    javaCode: `import java.util.ArrayDeque;
import java.util.Deque;

public class DequeStack {
    public static void main(String[] args) {
        Deque<Integer> stack = new ArrayDeque<>();

        stack.push(10);
        stack.push(20);
        stack.push(30);

        System.out.println("Peek: " + stack.peek());
        System.out.println("Pop: " + stack.pop());
        System.out.println("Size: " + stack.size());
    }
}`,
    interviewQuestions: [
      'What is the difference between Deque.push() and Deque.offer()?',
      'Can ArrayDeque hold null values?',
    ],
    commonMistakes: [
      'Confusing Deque.push() (adds to front, stack behavior) with Deque.offer() (adds to rear, queue behavior).',
      'ArrayDeque does not allow null elements — NullPointerException if pushed.',
    ],
    optimizedVersion: '// ArrayDeque has no capacity limit and resizes automatically.',
  },

  // ── Operations ───────────────────────────────────────────────────────────
  balancedParentheses: {
    name: 'Balanced Parentheses',
    className: 'BalancedParentheses',
    category: 'Algorithms',
    explanation:
      'Check if a string containing (, ), {, }, [, ] has correctly matched and nested parentheses. Push open brackets onto the stack; on closing bracket, verify the top matches.',
    timeComplexity: 'O(N)',
    spaceComplexity: 'O(N)',
    javaCode: `import java.util.Stack;

public class BalancedParentheses {
    public static void main(String[] args) {
        String s = "({[]})";
        Stack<Character> stack = new Stack<>();
        boolean valid = true;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(' || c == '{' || c == '[') {
                stack.push(c);
            } else {
                if (stack.isEmpty()) { valid = false; break; }
                char top = stack.pop();
                if ((c == ')' && top != '(') ||
                    (c == '}' && top != '{') ||
                    (c == ']' && top != '[')) {
                    valid = false;
                    break;
                }
            }
        }
        if (!stack.isEmpty()) valid = false;
        System.out.println("Valid: " + valid);
    }
}`,
    interviewQuestions: [
      'What happens if the string is empty?',
      'How would you extend this to handle HTML tags?',
    ],
    commonMistakes: [
      'Forgetting to check stack.isEmpty() AFTER the loop — open brackets with no closing will leave stack non-empty.',
      'Returning true without checking that the stack is empty at the end.',
    ],
    optimizedVersion: '// The algorithm is already optimal at O(N) time and O(N) space.',
  },

  nge: {
    name: 'Next Greater Element',
    className: 'NextGreaterElement',
    category: 'Algorithms',
    explanation:
      'For each element, find the first greater element to its right using a monotonic stack. The stack stores indices of unresolved elements. When a greater element is found, pop and assign NGE.',
    timeComplexity: 'O(N)',
    spaceComplexity: 'O(N)',
    javaCode: `import java.util.Stack;
import java.util.Arrays;

public class NextGreaterElement {
    public static void main(String[] args) {
        int[] arr = {4, 5, 2, 10, 8};
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
        System.out.println(Arrays.toString(nge));
    }
}`,
    interviewQuestions: [
      'How would you find the NGE for a circular array?',
      'What is a monotonic stack?',
    ],
    commonMistakes: [
      'Storing values instead of indices — you need indices to map results back to the original array.',
      'Iterating right-to-left when the left-to-right approach is more intuitive for NGE.',
    ],
    optimizedVersion: '// Already O(N) — each element is pushed and popped at most once.',
  },

  pge: {
    name: 'Previous Greater Element',
    className: 'PreviousGreaterElement',
    category: 'Algorithms',
    explanation:
      'For each element, find the first greater element to its left. Use a stack that maintains a monotonically decreasing sequence of values. Pop elements smaller than or equal to current.',
    timeComplexity: 'O(N)',
    spaceComplexity: 'O(N)',
    javaCode: `import java.util.Stack;
import java.util.Arrays;

public class PreviousGreaterElement {
    public static void main(String[] args) {
        int[] arr = {4, 5, 2, 10, 8};
        int n = arr.length;
        int[] pge = new int[n];
        Arrays.fill(pge, -1);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && stack.peek() <= arr[i]) {
                stack.pop();
            }
            pge[i] = stack.isEmpty() ? -1 : stack.peek();
            stack.push(arr[i]);
        }
        System.out.println(Arrays.toString(pge));
    }
}`,
    interviewQuestions: [
      'How does PGE differ from NGE in the traversal direction?',
      'What data structure best solves PGE? Why not a simple linear scan?',
    ],
    commonMistakes: [
      'Storing indices instead of values (for PGE you typically store values).',
      'Using > instead of >= causes incorrect handling of equal values.',
    ],
    optimizedVersion: '// O(N) using a monotonic decreasing stack.',
  },

  nse: {
    name: 'Next Smaller Element',
    className: 'NextSmallerElement',
    category: 'Algorithms',
    explanation:
      'For each element, find the first smaller element to its right. Traverse from right to left. Use a stack maintaining a monotonically increasing sequence.',
    timeComplexity: 'O(N)',
    spaceComplexity: 'O(N)',
    javaCode: `import java.util.Stack;
import java.util.Arrays;

public class NextSmallerElement {
    public static void main(String[] args) {
        int[] arr = {4, 5, 2, 10, 8};
        int n = arr.length;
        int[] nse = new int[n];
        Arrays.fill(nse, -1);
        Stack<Integer> stack = new Stack<>();

        for (int i = n - 1; i >= 0; i--) {
            while (!stack.isEmpty() && stack.peek() >= arr[i]) {
                stack.pop();
            }
            nse[i] = stack.isEmpty() ? -1 : stack.peek();
            stack.push(arr[i]);
        }
        System.out.println(Arrays.toString(nse));
    }
}`,
    interviewQuestions: [
      'What is the relation between NSE and "Largest Rectangle in Histogram"?',
      'Can NSE be computed using a left-to-right traversal?',
    ],
    commonMistakes: [
      'Not traversing in the correct direction (right-to-left for NSE).',
      'Forgetting to handle the case where the stack is empty (no smaller element → -1).',
    ],
    optimizedVersion: '// Also solvable left-to-right by tracking unfulfilled indices.',
  },

  infixToPostfix: {
    name: 'Infix to Postfix',
    className: 'InfixToPostfix',
    category: 'Algorithms',
    explanation:
      'Convert an infix expression (a+b*c) to postfix (abc*+) using a stack for operators. Operands go directly to output; operators are pushed/popped based on precedence.',
    timeComplexity: 'O(N)',
    spaceComplexity: 'O(N)',
    javaCode: `import java.util.Stack;

public class InfixToPostfix {
    static int precedence(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        return 0;
    }

    public static void main(String[] args) {
        String infix = "a+b*c-d";
        Stack<Character> stack = new Stack<>();
        StringBuilder postfix = new StringBuilder();

        for (int i = 0; i < infix.length(); i++) {
            char c = infix.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                postfix.append(c);
            } else if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    postfix.append(stack.pop());
                }
                stack.pop();
            } else {
                while (!stack.isEmpty() && precedence(stack.peek()) >= precedence(c)) {
                    postfix.append(stack.pop());
                }
                stack.push(c);
            }
        }
        while (!stack.isEmpty()) postfix.append(stack.pop());
        System.out.println(postfix);
    }
}`,
    interviewQuestions: [
      'How do you handle right-associative operators like ^ (power)?',
      'What is the difference between infix-to-postfix and infix-to-prefix conversions?',
    ],
    commonMistakes: [
      'Using >= for precedence comparison makes +, -, *, / right-associative. Use > for right-to-left associativity.',
      'Forgetting to pop remaining operators from the stack after the loop.',
    ],
    optimizedVersion: '// The Shunting-Yard algorithm is already the standard optimal approach.',
  },

  postfixEval: {
    name: 'Postfix Evaluation',
    className: 'PostfixEval',
    category: 'Algorithms',
    explanation:
      'Evaluate a postfix expression like "231*+9-". Push operands onto the stack. On encountering an operator, pop two operands, compute the result, and push it back.',
    timeComplexity: 'O(N)',
    spaceComplexity: 'O(N)',
    javaCode: `import java.util.Stack;

public class PostfixEval {
    public static void main(String[] args) {
        String expr = "231*+9-";
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (Character.isDigit(c)) {
                stack.push(c - '0');
            } else {
                int b = stack.pop();
                int a = stack.pop();
                if (c == '+') stack.push(a + b);
                else if (c == '-') stack.push(a - b);
                else if (c == '*') stack.push(a * b);
                else stack.push(a / b);
            }
        }
        System.out.println("Result: " + stack.pop());
    }
}`,
    interviewQuestions: [
      'Why is operand order important in subtraction and division?',
      'How would you handle multi-digit numbers in postfix evaluation?',
    ],
    commonMistakes: [
      'Popping b before a — the first pop is b (top), second pop is a (bottom of the pair).',
      'Not handling division by zero.',
    ],
    optimizedVersion: '// Use a char[] for faster iteration on large expressions.',
  },

  minStack: {
    name: 'Min Stack',
    className: 'MinStack',
    category: 'Algorithms',
    explanation:
      'Design a stack supporting O(1) push, pop, peek, and getMin(). Uses an auxiliary "min stack" that tracks the current minimum at each state of the main stack.',
    timeComplexity: 'O(1) for all operations',
    spaceComplexity: 'O(N)',
    javaCode: `import java.util.Stack;

public class MinStack {
    Stack<Integer> stack = new Stack<>();
    Stack<Integer> minSt = new Stack<>();

    void push(int x) {
        stack.push(x);
        if (minSt.isEmpty() || x <= minSt.peek()) {
            minSt.push(x);
        }
    }

    int pop() {
        int val = stack.pop();
        if (val == minSt.peek()) minSt.pop();
        return val;
    }

    int getMin() { return minSt.peek(); }

    public static void main(String[] args) {
        MinStack ms = new MinStack();
        ms.push(5); ms.push(3); ms.push(7); ms.push(1);
        System.out.println("Min: " + ms.getMin());
        ms.pop();
        System.out.println("Min: " + ms.getMin());
    }
}`,
    interviewQuestions: [
      'What happens if duplicate minimums are pushed? Do you push them to minSt twice?',
      'Can you implement MinStack using a single stack with pairs (value, currentMin)?',
    ],
    commonMistakes: [
      'Using < instead of <= when pushing to minSt — duplicates must be tracked to correctly restore after pop.',
      'Comparing Integer objects with == instead of .equals() for autoboxed values.',
    ],
    optimizedVersion: '// Single-stack variant: push (val, currentMin) pairs to avoid extra space overhead.',
  },

  reverseStack: {
    name: 'Reverse Stack',
    className: 'StackReverse',
    category: 'Algorithms',
    explanation:
      'Reverse a stack using only recursive push/pop operations without an auxiliary array. Uses insertAtBottom() to place the popped top element at the bottom after each recursion.',
    timeComplexity: 'O(N²)',
    spaceComplexity: 'O(N) call stack',
    javaCode: `import java.util.Stack;

public class StackReverse {
    static void insertAtBottom(Stack<Integer> st, int x) {
        if (st.isEmpty()) { st.push(x); return; }
        int top = st.pop();
        insertAtBottom(st, x);
        st.push(top);
    }

    static void reverse(Stack<Integer> st) {
        if (st.isEmpty()) return;
        int top = st.pop();
        reverse(st);
        insertAtBottom(st, top);
    }

    public static void main(String[] args) {
        Stack<Integer> st = new Stack<>();
        st.push(1); st.push(2); st.push(3);
        System.out.println("Before: " + st);
        reverse(st);
        System.out.println("After: " + st);
    }
}`,
    interviewQuestions: [
      'Why is Reverse Stack O(N²) instead of O(N)?',
      'How would you reverse a stack in O(N) using an auxiliary data structure?',
    ],
    commonMistakes: [
      'Not returning after st.push(x) in the base case causes incorrect behavior.',
      'Calling reverse() instead of insertAtBottom() recursively can cause infinite loops.',
    ],
    optimizedVersion: `// O(N) using a temporary queue or list:
// List<Integer> temp = new ArrayList<>(stack);
// Collections.reverse(temp);
// stack.clear(); temp.forEach(stack::push);`,
  },
};

// ── Comparison Templates ──────────────────────────────────────────────────────
// Used by the Comparison tab to run two algorithms side-by-side.

export const COMPARISON_TEMPLATES = {
  // Stack: Array-backed vs java.util.Stack
  arrayStack: {
    label: 'Array Stack',
    className: 'StackArray',
    time: 'O(1)',
    space: 'O(N) fixed',
    javaCode: `public class StackArray {
    static int[] arr = new int[5];
    static int top = -1;
    static void push(int x) { if (top < arr.length - 1) arr[++top] = x; }
    static int pop() { return top >= 0 ? arr[top--] : -1; }
    public static void main(String[] args) {
        int iterations = 0;
        push(10); iterations++;
        push(20); iterations++;
        push(30); iterations++;
        pop();    iterations++;
        pop();    iterations++;
        System.out.println("Iterations: " + iterations);
    }
}`,
  },

  javaStack: {
    label: 'Java Stack',
    className: 'JavaStackComp',
    time: 'O(1) amortized',
    space: 'O(N) dynamic',
    javaCode: `import java.util.Stack;
public class JavaStackComp {
    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<>();
        int iterations = 0;
        stack.push(10); iterations++;
        stack.push(20); iterations++;
        stack.push(30); iterations++;
        stack.pop();    iterations++;
        stack.pop();    iterations++;
        System.out.println("Iterations: " + iterations);
    }
}`,
  },

  // Stack: Balanced Parentheses vs NGE (different use-cases side-by-side)
  balancedParen: {
    label: 'Balanced Parentheses',
    className: 'BalancedParentheses',
    time: 'O(N)',
    space: 'O(N)',
    javaCode: `import java.util.Stack;
public class BalancedParentheses {
    public static void main(String[] args) {
        String s = "({[]})";
        Stack<Character> stack = new Stack<>();
        boolean valid = true;
        for (char c : s.toCharArray()) {
            if (c == '(' || c == '{' || c == '[') { stack.push(c); }
            else {
                if (stack.isEmpty()) { valid = false; break; }
                char top = stack.pop();
                if ((c==')' && top!='(') || (c=='}' && top!='{') || (c==']' && top!='[')) { valid = false; break; }
            }
        }
        System.out.println("Valid: " + (valid && stack.isEmpty()));
    }
}`,
  },

  nge: {
    label: 'Next Greater Element',
    className: 'NextGreaterElement',
    time: 'O(N)',
    space: 'O(N)',
    javaCode: `import java.util.Stack;
import java.util.Arrays;
public class NextGreaterElement {
    public static void main(String[] args) {
        int[] arr = {4, 5, 2, 10, 8};
        int n = arr.length;
        int[] nge = new int[n];
        Arrays.fill(nge, -1);
        Stack<Integer> stack = new Stack<>();
        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && arr[stack.peek()] < arr[i]) nge[stack.pop()] = arr[i];
            stack.push(i);
        }
        System.out.println(Arrays.toString(nge));
    }
}`,
  },
};

export const STACK_QUIZ_QUESTIONS = [
  {
    type: 'MCQ',
    question: 'What is the time complexity of push and pop operations on a stack implemented using an array (with sufficient capacity)?',
    options: ['O(1)', 'O(log N)', 'O(N)', 'O(N²)'],
    answer: 'O(1)',
    explanation: 'Array-backed push/pop only increments/decrements a top pointer and accesses arr[top], which is a constant-time O(1) operation.',
  },
  {
    type: 'MCQ',
    question: 'Which Java class is preferred over java.util.Stack for stack operations in modern single-threaded code?',
    options: ['java.util.LinkedList', 'java.util.ArrayDeque', 'java.util.PriorityQueue', 'java.util.Vector'],
    answer: 'java.util.ArrayDeque',
    explanation: 'java.util.Stack extends Vector and all methods are synchronized, adding unnecessary overhead in single-threaded contexts. ArrayDeque provides the same LIFO interface without synchronization.',
  },
  {
    type: 'PREDICTION',
    question: 'What does the Balanced Parentheses algorithm return for the input "({[}])"?',
    options: ['true — all brackets are present', 'false — brackets are mismatched', 'StackOverflowError', 'NullPointerException'],
    answer: 'false — brackets are mismatched',
    explanation: 'When the algorithm encounters ] after pushing {, it pops { and checks if ] matches { — it does not, so valid = false. The brackets are present but incorrectly nested.',
  },
  {
    type: 'MCQ',
    question: 'In the Min Stack design using two stacks, what is the condition for pushing to the auxiliary min stack?',
    options: [
      'Push if the new element is less than the top of minStack',
      'Push if the new element is less than or equal to the top of minStack',
      'Always push to minStack',
      'Push only if minStack is empty',
    ],
    answer: 'Push if the new element is less than or equal to the top of minStack',
    explanation: 'Using <= handles duplicate minimum values correctly. If you used <, popping a duplicate minimum would incorrectly update the tracked minimum.',
  },
  {
    type: 'DRYRUN',
    question: 'Trace the NGE algorithm on arr = [4, 5]. After processing index 0 (value=4): What is in the stack? After processing index 1 (value=5): What does nge[0] equal?',
    options: [
      'Stack = [0]; nge[0] = 5',
      'Stack = [1]; nge[0] = 4',
      'Stack = []; nge[0] = -1',
      'Stack = [0, 1]; nge[0] = -1',
    ],
    answer: 'Stack = [0]; nge[0] = 5',
    explanation: 'After index 0: push 0 → stack=[0]. At index 1: arr[stack.peek()]=arr[0]=4 < arr[1]=5, so pop 0 and set nge[0]=5. Then push 1 → stack=[1]. nge[0] = 5.',
  },
];
