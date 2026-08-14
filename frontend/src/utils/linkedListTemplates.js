export const LINKED_LIST_OPERATIONS = {
  singlyList: {
    name: "Singly Linked List",
    className: "SinglyLinkedList",
    explanation: "A Singly Linked List consists of nodes where each node contains data and a reference (next) to the next node in the list. The last node points to null.",
    timeComplexity: "O(N) search, O(1) insert",
    spaceComplexity: "O(N) total",
    javaCode: `public class SinglyLinkedList {
    static class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }
    
    public static void main(String[] args) {
        Node head = new Node(10);
        head.next = new Node(20);
        head.next.next = new Node(30);
        
        Node current = head;
        while (current != null) {
            System.out.print(current.data + " -> ");
            current = current.next;
        }
        System.out.println("null");
    }
}`,
    interviewQuestions: [
      "Find the length of a singly linked list.",
      "Print list values in reverse order."
    ],
    commonMistakes: [
      "Losing the head pointer reference by modifying it during traversal.",
      "NullPointerException when attempting to access next on a null node reference."
    ],
    optimizedVersion: `// LinkedList is space-efficient as it allocates memory dynamically for nodes.`
  },

  doublyList: {
    name: "Doubly Linked List",
    className: "DoublyLinkedList",
    explanation: "A Doubly Linked List consists of nodes containing data, a reference to the next node, and a reference to the previous node, allowing traversal in both directions.",
    timeComplexity: "O(N) search, O(1) insert",
    spaceComplexity: "O(N)",
    javaCode: `public class DoublyLinkedList {
    static class Node {
        int data;
        Node next, prev;
        Node(int data) { this.data = data; }
    }
    
    public static void main(String[] args) {
        Node head = new Node(10);
        Node second = new Node(20);
        head.next = second;
        second.prev = head;
        
        Node current = head;
        while (current != null) {
            System.out.println("Node: " + current.data);
            current = current.next;
        }
    }
}`,
    interviewQuestions: [
      "Reverse a doubly linked list.",
      "Delete a node in a doubly linked list without traversing."
    ],
    commonMistakes: [
      "Forgetting to update the prev pointer when rerouting next references.",
      "NullPointerException at boundaries (head or tail)."
    ],
    optimizedVersion: `// Doubly Linked List allows deletion in O(1) if target node reference is given.`
  },

  insertBegin: {
    name: "Insert Begin",
    className: "LinkedListInsertBegin",
    explanation: "Inserting a node at the head of a linked list is a constant-time O(1) operation because it only requires updating the new node's next pointer to point to the current head.",
    timeComplexity: "O(1)",
    spaceComplexity: "O(1)",
    javaCode: `public class LinkedListInsertBegin {
    static class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }
    
    public static void main(String[] args) {
        Node head = new Node(20);
        head.next = new Node(30);
        
        Node newNode = new Node(10);
        newNode.next = head;
        head = newNode;
        
        System.out.println("Head value: " + head.data);
    }
}`,
    interviewQuestions: [
      "Implement a queue using a linked list with head and tail pointers.",
      "Insert at index K in a linked list."
    ],
    commonMistakes: [
      "Assigning head before pointing newNode.next to head, which results in losing references to the rest of the list."
    ],
    optimizedVersion: `// Insert at start is always O(1).`
  },

  deleteNode: {
    name: "Delete Node",
    className: "LinkedListDelete",
    explanation: "Deleting a node in a singly linked list requires locating the node just before the target node and updating its next reference to skip the target node.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class LinkedListDelete {
    static class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }
    
    public static void main(String[] args) {
        Node head = new Node(10);
        head.next = new Node(20);
        head.next.next = new Node(30);
        
        Node current = head;
        if (current.next != null && current.next.data == 20) {
            current.next = current.next.next;
        }
    }
}`,
    interviewQuestions: [
      "Delete a node in a singly linked list given only a reference to that node.",
      "Remove duplicates from a sorted linked list."
    ],
    commonMistakes: [
      "Failing to garbage collect the deleted node (automatically handled in Java, but critical to update references properly).",
      "Not handling deletion of the head node correctly."
    ],
    optimizedVersion: `// If head needs deletion, assign head = head.next.`
  },

  reverse: {
    name: "Reverse List",
    className: "LinkedListReverse",
    explanation: "Reversing a linked list involves iterating through the list and swapping next pointers for each node to point to their previous node.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class LinkedListReverse {
    static class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }
    
    public static void main(String[] args) {
        Node head = new Node(1);
        head.next = new Node(2);
        head.next.next = new Node(3);
        
        Node prev = null;
        Node curr = head;
        while (curr != null) {
            Node next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        head = prev;
        System.out.println("Reversed Head: " + head.data);
    }
}`,
    interviewQuestions: [
      "Reverse a linked list using recursion.",
      "Reverse a sublist of a linked list from position m to n."
    ],
    commonMistakes: [
      "Attempting to shift curr before storing original next node reference, resulting in breaking traversal loop."
    ],
    optimizedVersion: `// Iterative is optimal as recursion uses O(N) stack frame space.`
  },

  cycle: {
    name: "Detect Cycle",
    className: "LinkedListCycle",
    explanation: "Floyd's Tortoise and Hare algorithm detects cycle in a linked list using two pointers moving at different speeds. If a cycle exists, they will eventually meet.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class LinkedListCycle {
    static class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }
    
    public static void main(String[] args) {
        Node head = new Node(1);
        head.next = new Node(2);
        head.next.next = new Node(3);
        head.next.next.next = head.next; // Cycle
        
        Node slow = head;
        Node fast = head;
        boolean hasCycle = false;
        
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {
                hasCycle = true;
                break;
            }
        }
        System.out.println("Has Cycle: " + hasCycle);
    }
}`,
    interviewQuestions: [
      "Find the starting node of the cycle in a linked list.",
      "Remove the cycle from a linked list."
    ],
    commonMistakes: [
      "Accessing fast.next.next without verifying that fast and fast.next are non-null."
    ],
    optimizedVersion: `// Floyd's algorithm is optimal with O(1) space.`
  }
};

export const COMPARISON_TEMPLATES = {
  reverse: {
    className: "LinkedListReverse",
    javaCode: `public class LinkedListReverse {
    static class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }
    public static void main(String[] args) {
        Node head = new Node(1);
        head.next = new Node(2);
        head.next.next = new Node(3);
        Node prev = null;
        Node curr = head;
        while (curr != null) {
            Node next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        head = prev;
        System.out.println("Reversed Head: " + head.data);
    }
}`
  },
  cycle: {
    className: "LinkedListCycle",
    javaCode: `public class LinkedListCycle {
    static class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }
    public static void main(String[] args) {
        Node head = new Node(1);
        head.next = new Node(2);
        head.next.next = new Node(3);
        head.next.next.next = head.next;
        Node slow = head;
        Node fast = head;
        boolean hasCycle = false;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {
                hasCycle = true;
                break;
            }
        }
        System.out.println("Has Cycle: " + hasCycle);
    }
}`
  }
};
