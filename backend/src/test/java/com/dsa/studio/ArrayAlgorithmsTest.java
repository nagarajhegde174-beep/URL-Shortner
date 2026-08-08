package com.dsa.studio;

import com.dsa.studio.dto.response.StepDebugInfo;
import com.dsa.studio.service.CompilerService;
import com.dsa.studio.service.JdiDebuggerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ArrayAlgorithmsTest {

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
    public void testEmptyArray() {
        String code = "public class EmptyArray {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {};\n" +
                "        int iterations = 0;\n" +
                "        for (int i = 0; i < arr.length; i++) {\n" +
                "            iterations++;\n" +
                "        }\n" +
                "        System.out.println(\"Done: \" + iterations);\n" +
                "    }\n" +
                "}";
        List<StepDebugInfo> trace = runCode("EmptyArray", code);
        assertFalse(trace.isEmpty(), "Trace must not be empty");
        StepDebugInfo last = trace.get(trace.size() - 1);
        assertEquals("Done: 0\n", last.getOutput());
    }

    @Test
    public void testSingleElementArray() {
        String code = "public class SingleElement {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {42};\n" +
                "        int max = arr[0];\n" +
                "        System.out.println(\"Max: \" + max);\n" +
                "    }\n" +
                "}";
        List<StepDebugInfo> trace = runCode("SingleElement", code);
        assertFalse(trace.isEmpty());
        StepDebugInfo last = trace.get(trace.size() - 1);
        assertEquals("Max: 42\n", last.getOutput());
    }

    @Test
    public void testDuplicateValues() {
        String code = "public class DuplicateValues {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {5, 5, 5, 5};\n" +
                "        int count = 0;\n" +
                "        for (int x : arr) {\n" +
                "            if (x == 5) count++;\n" +
                "        }\n" +
                "        System.out.println(\"Count: \" + count);\n" +
                "    }\n" +
                "}";
        List<StepDebugInfo> trace = runCode("DuplicateValues", code);
        assertFalse(trace.isEmpty());
        StepDebugInfo last = trace.get(trace.size() - 1);
        assertEquals("Count: 4\n", last.getOutput());
    }

    @Test
    public void testNegativeValues() {
        String code = "public class NegativeValues {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {-5, -12, -3, -8};\n" +
                "        int max = arr[0];\n" +
                "        for (int i = 1; i < arr.length; i++) {\n" +
                "            if (arr[i] > max) max = arr[i];\n" +
                "        }\n" +
                "        System.out.println(\"Max: \" + max);\n" +
                "    }\n" +
                "}";
        List<StepDebugInfo> trace = runCode("NegativeValues", code);
        assertFalse(trace.isEmpty());
        StepDebugInfo last = trace.get(trace.size() - 1);
        assertEquals("Max: -3\n", last.getOutput());
    }

    @Test
    public void testAlreadySortedArray() {
        String code = "public class AlreadySorted {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {1, 2, 3, 4, 5};\n" +
                "        boolean sorted = true;\n" +
                "        for (int i = 0; i < arr.length - 1; i++) {\n" +
                "            if (arr[i] > arr[i+1]) sorted = false;\n" +
                "        }\n" +
                "        System.out.println(\"Sorted: \" + sorted);\n" +
                "    }\n" +
                "}";
        List<StepDebugInfo> trace = runCode("AlreadySorted", code);
        assertFalse(trace.isEmpty());
        StepDebugInfo last = trace.get(trace.size() - 1);
        assertEquals("Sorted: true\n", last.getOutput());
    }

    @Test
    public void testReverseSortedArray() {
        String code = "public class ReverseSorted {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {5, 4, 3, 2, 1};\n" +
                "        int swaps = 0;\n" +
                "        for (int i = 0; i < arr.length - 1; i++) {\n" +
                "            if (arr[i] > arr[i+1]) {\n" +
                "                int temp = arr[i];\n" +
                "                arr[i] = arr[i+1];\n" +
                "                arr[i+1] = temp;\n" +
                "                swaps++;\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println(\"Swaps: \" + swaps);\n" +
                "    }\n" +
                "}";
        List<StepDebugInfo> trace = runCode("ReverseSorted", code);
        assertFalse(trace.isEmpty());
        StepDebugInfo last = trace.get(trace.size() - 1);
        assertEquals("Swaps: 4\n", last.getOutput());
    }

    @Test
    public void testLargeArray() {
        String code = "public class LargeArray {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = new int[100];\n" +
                "        for (int i = 0; i < arr.length; i++) {\n" +
                "            arr[i] = i;\n" +
                "        }\n" +
                "        System.out.println(\"Large array initialized: \" + arr.length);\n" +
                "    }\n" +
                "}";
        List<StepDebugInfo> trace = runCode("LargeArray", code);
        assertFalse(trace.isEmpty());
        StepDebugInfo last = trace.get(trace.size() - 1);
        assertEquals("Large array initialized: 100\n", last.getOutput());
    }
}
