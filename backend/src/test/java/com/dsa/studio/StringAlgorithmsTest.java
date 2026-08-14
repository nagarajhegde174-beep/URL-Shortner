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
public class StringAlgorithmsTest {

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
    public void testPalindrome() {
        String code = "public class PalindromeCheck {\n" +
                "    public static void main(String[] args) {\n" +
                "        String str = \"racecar\";\n" +
                "        int left = 0;\n" +
                "        int right = str.length() - 1;\n" +
                "        boolean isPal = true;\n" +
                "        while (left < right) {\n" +
                "            if (str.charAt(left) != str.charAt(right)) {\n" +
                "                isPal = false;\n" +
                "                break;\n" +
                "            }\n" +
                "            left++;\n" +
                "            right--;\n" +
                "        }\n" +
                "        System.out.println(\"Is Palindrome: \" + isPal);\n" +
                "    }\n" +
                "}";
        List<StepDebugInfo> trace = runCode("PalindromeCheck", code);
        assertFalse(trace.isEmpty());
        StepDebugInfo last = trace.get(trace.size() - 1);
        assertEquals("Is Palindrome: true\n", last.getOutput());
        
        // Assert that STRING metadata was generated
        boolean hasStringMetadata = trace.stream().anyMatch(step -> 
            step.getMetadata() != null && "STRING".equals(step.getMetadata().getDataStructure())
        );
        assertTrue(hasStringMetadata, "Should have STRING dataStructure metadata");
    }

    @Test
    public void testAnagram() {
        String code = "public class AnagramCheck {\n" +
                "    public static void main(String[] args) {\n" +
                "        String s1 = \"listen\";\n" +
                "        String s2 = \"silent\";\n" +
                "        boolean isAnagram = true;\n" +
                "        if (s1.length() != s2.length()) {\n" +
                "            isAnagram = false;\n" +
                "        } else {\n" +
                "            int[] counts = new int[26];\n" +
                "            for (int i = 0; i < s1.length(); i++) {\n" +
                "                counts[s1.charAt(i) - 'a']++;\n" +
                "                counts[s2.charAt(i) - 'a']--;\n" +
                "            }\n" +
                "            for (int c : counts) {\n" +
                "                if (c != 0) { isAnagram = false; break; }\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println(\"Is Anagram: \" + isAnagram);\n" +
                "    }\n" +
                "}";
        List<StepDebugInfo> trace = runCode("AnagramCheck", code);
        assertFalse(trace.isEmpty());
        StepDebugInfo last = trace.get(trace.size() - 1);
        assertEquals("Is Anagram: true\n", last.getOutput());
    }
}
