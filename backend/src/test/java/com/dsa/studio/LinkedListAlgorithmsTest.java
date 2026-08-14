package com.dsa.studio;

import com.dsa.studio.dto.response.StepDebugInfo;
import com.dsa.studio.service.CompilerService;
import com.dsa.studio.service.JdiDebuggerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class LinkedListAlgorithmsTest {

    @Autowired
    private CompilerService compilerService;

    @Autowired
    private JdiDebuggerService jdiDebuggerService;

    private List<StepDebugInfo> runCode(String className, String code) {
        var compileRes = compilerService.compile(className, code);
        assertTrue(compileRes.isSuccess(), "Compilation failed: " + compileRes.getErrors());
        return jdiDebuggerService.generateTrace(className, "");
    }

    @Test
    public void testLinkedListTraversal() {
        String code = "public class SinglyLinkedListTest {\n" +
                "    static class Node {\n" +
                "        int data;\n" +
                "        Node next;\n" +
                "        Node(int data) { this.data = data; }\n" +
                "    }\n" +
                "    public static void main(String[] args) {\n" +
                "        Node head = new Node(10);\n" +
                "        head.next = new Node(20);\n" +
                "        head.next.next = new Node(30);\n" +
                "        Node current = head;\n" +
                "        int count = 0;\n" +
                "        while (current != null) {\n" +
                "            count++;\n" +
                "            current = current.next;\n" +
                "        }\n" +
                "        System.out.println(\"Count: \" + count);\n" +
                "    }\n" +
                "}";
        List<StepDebugInfo> trace = runCode("SinglyLinkedListTest", code);
        assertFalse(trace.isEmpty());
        StepDebugInfo last = trace.get(trace.size() - 1);
        assertEquals("Count: 3\n", last.getOutput());

        // Assert that LINKED_LIST metadata and nodeSnapshot were generated
        boolean hasLinkedListMetadata = false;
        boolean hasSnapshot = false;

        for (StepDebugInfo step : trace) {
            if (step.getMetadata() != null && "LINKED_LIST".equals(step.getMetadata().getDataStructure())) {
                hasLinkedListMetadata = true;
                List<Map<String, String>> snapshot = step.getMetadata().getNodeSnapshot();
                if (snapshot != null && !snapshot.isEmpty()) {
                    hasSnapshot = true;
                    // Check if node data fields are extracted correctly
                    boolean hasCorrectDataVal = snapshot.stream().anyMatch(node -> "10".equals(node.get("data")));
                    assertTrue(hasCorrectDataVal, "Snapshot should contain node with data value 10");
                }
            }
        }
        assertTrue(hasLinkedListMetadata, "Should have LINKED_LIST dataStructure metadata");
        assertTrue(hasSnapshot, "Should have non-empty nodeSnapshot in steps");
    }
}
