package com.dsa.studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepMetadata {

    // ─── Shared Fields ────────────────────────────────────────────────────────
    /** The data structure being visualized: ARRAY, STRING, LINKED_LIST, STACK, QUEUE, etc. */
    private String dataStructure;

    /** The operation being performed at this step.
     *  Arrays:      READ, WRITE, COMPARE, SWAP, POINTER_MOVE, INSERT, DELETE, WINDOW_UPDATE
     *  Strings:     READ, COMPARE, MATCH, MISMATCH, POINTER_MOVE, WINDOW_UPDATE, PATTERN_SHIFT, HASH_COMPUTE
     *  LinkedLists: TRAVERSE, NODE_CREATE, NODE_DELETE, POINTER_UPDATE, INSERT, REMOVE, REVERSE, COMPARE
     *  Stacks:      PUSH, POP, PEEK, STACK_UPDATE, COMPARE, REMOVE, INSERT, POINTER_UPDATE
     *  Queues:      ENQUEUE, DEQUEUE, PEEK, FRONT_MOVE, REAR_MOVE, QUEUE_UPDATE, COMPARE, REMOVE, INSERT
     */
    private String operation;

    /** Integer indices of active elements (for array/string cell highlighting). */
    private List<Integer> indices;

    /** Named pointer positions: e.g. {"i": 3, "j": 7, "left": 0, "right": 9, "low": 2, "high": 5, "mid": 3} */
    private Map<String, Integer> pointers;

    // ─── String-specific Fields ───────────────────────────────────────────────
    /** Per-character state map: index → state token (MATCH, MISMATCH, WINDOW, PATTERN, ACTIVE, VISITED) */
    private Map<Integer, String> characterStates;

    /** LPS array values for KMP algorithm visualization (index → lps value) */
    private List<Integer> lpsArray;

    /** The pattern string being searched for (for pattern matching visualization) */
    private String pattern;

    /** Current pattern shift offset relative to text (for Naive/KMP/Rabin-Karp) */
    private Integer patternOffset;

    /** Current Rabin-Karp rolling hash value for display */
    private Long rollingHash;

    // ─── Linked List-specific Fields ─────────────────────────────────────────
    /** Unique node identifier (JDI object ID as string) for the node being acted on */
    private String nodeId;

    /** JDI object unique ID — the stable identity of a heap-allocated node object */
    private Long objectId;

    /** The nodeId this node's `next` pointer currently points to (null = NULL terminator) */
    private String nextNodeId;

    /** The nodeId this node's `prev` pointer currently points to (for doubly linked list) */
    private String previousNodeId;

    /** Named node pointer labels and their target nodeIds:
     *  e.g. {"head": "node_1", "current": "node_3", "slow": "node_2", "fast": "node_4"} */
    private Map<String, String> nodePointers;

    /** Snapshot of all current nodes in the list (for full re-render on each step).
     *  Each entry: {"nodeId": "n1", "data": "10", "nextNodeId": "n2", "prevNodeId": null} */
    private List<Map<String, String>> nodeSnapshot;

    // ─── Stack-specific Fields ────────────────────────────────────────────────
    /**
     * Ordered snapshot of the DSA stack contents, index 0 = TOP, last = BOTTOM.
     * Populated by extractStackMetadata() using this priority:
     *   1. Explicit JDI runtime state (inspect java.util.Stack / Deque backing array via JDI)
     *   2. Variable type inspection (find variables whose declared type includes "Stack"/"Deque")
     *   3. Class-name heuristic (final fallback)
     *
     * NOTE: This is the USER's DSA stack — completely separate from the JVM call stack
     * which is captured in StepDebugInfo.callStack.
     */
    private List<String> stackSnapshot;

    /** Current top-of-stack index (0-based, -1 = empty). */
    private Integer topIndex;

    /** Name of the stack variable in scope (e.g. "stack", "st", "myStack"). */
    private String stackVariableName;

    // ─── Queue-specific Fields ────────────────────────────────────────────────
    /**
     * Ordered snapshot of the DSA queue contents, index 0 = FRONT, last = REAR.
     * Populated by extractQueueMetadata() using the same priority as stackSnapshot:
     *   1. JDI runtime state inspection
     *   2. Variable type inspection
     *   3. Class-name heuristic fallback
     */
    private List<String> queueSnapshot;

    /** Current front pointer index in the backing array (for array-based circular queues). */
    private Integer frontIndex;

    /** Current rear pointer index in the backing array (for array-based circular queues). */
    private Integer rearIndex;

    /** Name of the queue variable in scope (e.g. "queue", "q"). */
    private String queueVariableName;

    /**
     * True when the queue variable is a java.util.PriorityQueue.
     * The QueueVisualizer uses this flag to apply priority-order rendering.
     * This flag also enables future integration with a HeapVisualizer
     * when the Heap/PriorityQueue module is added in a later phase.
     */
    private Boolean isPriorityQueue;
}
