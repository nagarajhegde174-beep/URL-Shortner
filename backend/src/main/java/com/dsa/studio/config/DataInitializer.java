package com.dsa.studio.config;

import com.dsa.studio.model.Algorithm;
import com.dsa.studio.model.ERole;
import com.dsa.studio.model.PracticeProblem;
import com.dsa.studio.model.Role;
import com.dsa.studio.repository.AlgorithmRepository;
import com.dsa.studio.repository.PracticeProblemRepository;
import com.dsa.studio.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AlgorithmRepository algorithmRepository;
    private final PracticeProblemRepository practiceProblemRepository;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Roles
        for (ERole role : ERole.values()) {
            if (roleRepository.findByName(role).isEmpty()) {
                Role newRole = new Role();
                newRole.setName(role);
                roleRepository.save(newRole);
                log.info("Created role: {}", role.name());
            }
        }

        // 2. Seed Algorithms
        if (algorithmRepository.count() == 0) {
            seedAlgorithms();
        }
        // Seed String and Linked List algorithms if not yet seeded
        if (algorithmRepository.countByCategory("STRING") == 0) {
            seedStringAlgorithms();
        }
        if (algorithmRepository.countByCategory("LINKED_LIST") == 0) {
            seedLinkedListAlgorithms();
        }
        if (algorithmRepository.countByCategory("STACK") == 0) {
            seedStackAlgorithms();
        }
        if (algorithmRepository.countByCategory("QUEUE") == 0) {
            seedQueueAlgorithms();
        }

        // 3. Seed Practice Problems
        if (practiceProblemRepository.count() == 0) {
            seedPracticeProblems();
        }
        // Seed String and Linked List practice problems if not yet seeded
        if (practiceProblemRepository.countByCategory("STRING") == 0) {
            seedStringPracticeProblems();
        }
        if (practiceProblemRepository.countByCategory("LINKED_LIST") == 0) {
            seedLinkedListPracticeProblems();
        }
        if (practiceProblemRepository.countByCategory("STACK") == 0) {
            seedStackPracticeProblems();
        }
        if (practiceProblemRepository.countByCategory("QUEUE") == 0) {
            seedQueuePracticeProblems();
        }
    }

    private void seedAlgorithms() {
        log.info("Seeding array algorithms...");

        // Operations
        saveAlgorithm("Traversal", "Iterate through each element of the array sequentially.", "ARRAY", "O(N)", "O(1)", "EASY",
                "public class ArrayTraversal {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {12, 34, 45, 9, 8, 90, 3};\n" +
                "        int iterations = 0;\n" +
                "        for (int i = 0; i < arr.length; i++) {\n" +
                "            iterations++;\n" +
                "            System.out.println(\"Element at index \" + i + \": \" + arr[i]);\n" +
                "        }\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Insert", "Insert a new element at a specific index, shifting subsequent elements to the right.", "ARRAY", "O(N)", "O(1)", "EASY",
                "public class ArrayInsert {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {10, 20, 30, 40, 50, 0};\n" +
                "        int size = 5;\n" +
                "        int element = 25;\n" +
                "        int insertIndex = 2;\n" +
                "        int iterations = 0;\n" +
                "        int swaps = 0;\n" +
                "        for (int i = size - 1; i >= insertIndex; i--) {\n" +
                "            iterations++;\n" +
                "            arr[i + 1] = arr[i];\n" +
                "            swaps++;\n" +
                "        }\n" +
                "        arr[insertIndex] = element;\n" +
                "        System.out.println(\"Inserted element successfully.\");\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Delete", "Delete an element at a given index, shifting all subsequent elements to the left.", "ARRAY", "O(N)", "O(1)", "EASY",
                "public class ArrayDelete {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {10, 20, 30, 40, 50};\n" +
                "        int deleteIndex = 2;\n" +
                "        int iterations = 0;\n" +
                "        int swaps = 0;\n" +
                "        for (int i = deleteIndex; i < arr.length - 1; i++) {\n" +
                "            iterations++;\n" +
                "            arr[i] = arr[i + 1];\n" +
                "            swaps++;\n" +
                "        }\n" +
                "        arr[arr.length - 1] = 0;\n" +
                "        System.out.println(\"Deleted element successfully.\");\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Update", "Modify the value of an element at a specific index.", "ARRAY", "O(1)", "O(1)", "EASY",
                "public class ArrayUpdate {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {10, 20, 30, 40, 50};\n" +
                "        int updateIndex = 3;\n" +
                "        int newValue = 99;\n" +
                "        arr[updateIndex] = newValue;\n" +
                "        System.out.println(\"Updated value successfully.\");\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Search", "Linear search to locate an element and return its index.", "ARRAY", "O(N)", "O(1)", "EASY",
                "public class ArraySearch {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {12, 34, 45, 9, 8, 90, 3};\n" +
                "        int target = 9;\n" +
                "        int index = -1;\n" +
                "        int iterations = 0;\n" +
                "        int comparisons = 0;\n" +
                "        for (int i = 0; i < arr.length; i++) {\n" +
                "            iterations++;\n" +
                "            comparisons++;\n" +
                "            if (arr[i] == target) {\n" +
                "                index = i;\n" +
                "                break;\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println(\"Target found at: \" + index);\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Reverse", "Reverse the order of elements in the array using two pointers.", "ARRAY", "O(N)", "O(1)", "EASY",
                "public class ArrayReverse {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {1, 2, 3, 4, 5, 6};\n" +
                "        int start = 0;\n" +
                "        int end = arr.length - 1;\n" +
                "        int iterations = 0;\n" +
                "        int swaps = 0;\n" +
                "        while (start < end) {\n" +
                "            iterations++;\n" +
                "            int temp = arr[start];\n" +
                "            arr[start] = arr[end];\n" +
                "            arr[end] = temp;\n" +
                "            swaps++;\n" +
                "            start++;\n" +
                "            end--;\n" +
                "        }\n" +
                "        System.out.println(\"Array reversed.\");\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Rotate Left", "Rotate the array left by 1 position (shifts all elements to the left, first wraps to end).", "ARRAY", "O(N)", "O(1)", "EASY",
                "public class ArrayRotateLeft {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {1, 2, 3, 4, 5};\n" +
                "        int iterations = 0;\n" +
                "        int swaps = 0;\n" +
                "        int first = arr[0];\n" +
                "        for (int i = 0; i < arr.length - 1; i++) {\n" +
                "            iterations++;\n" +
                "            arr[i] = arr[i + 1];\n" +
                "            swaps++;\n" +
                "        }\n" +
                "        arr[arr.length - 1] = first;\n" +
                "        System.out.println(\"Rotated left.\");\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Rotate Right", "Rotate the array right by 1 position (shifts all elements to the right, last wraps to start).", "ARRAY", "O(N)", "O(1)", "EASY",
                "public class ArrayRotateRight {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {1, 2, 3, 4, 5};\n" +
                "        int iterations = 0;\n" +
                "        int swaps = 0;\n" +
                "        int last = arr[arr.length - 1];\n" +
                "        for (int i = arr.length - 1; i > 0; i--) {\n" +
                "            iterations++;\n" +
                "            arr[i] = arr[i - 1];\n" +
                "            swaps++;\n" +
                "        }\n" +
                "        arr[0] = last;\n" +
                "        System.out.println(\"Rotated right.\");\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Maximum", "Find the maximum element value in the array.", "ARRAY", "O(N)", "O(1)", "EASY",
                "public class ArrayMax {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {12, 34, 45, 9, 8, 90, 3};\n" +
                "        int max = arr[0];\n" +
                "        int iterations = 0;\n" +
                "        int comparisons = 0;\n" +
                "        for (int i = 1; i < arr.length; i++) {\n" +
                "            iterations++;\n" +
                "            comparisons++;\n" +
                "            if (arr[i] > max) {\n" +
                "                max = arr[i];\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println(\"Max value: \" + max);\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Minimum", "Find the minimum element value in the array.", "ARRAY", "O(N)", "O(1)", "EASY",
                "public class ArrayMin {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {12, 34, 45, 9, 8, 90, 3};\n" +
                "        int min = arr[0];\n" +
                "        int iterations = 0;\n" +
                "        int comparisons = 0;\n" +
                "        for (int i = 1; i < arr.length; i++) {\n" +
                "            iterations++;\n" +
                "            comparisons++;\n" +
                "            if (arr[i] < min) {\n" +
                "                min = arr[i];\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println(\"Min value: \" + min);\n" +
                "    }\n" +
                "}");

        // Patterns
        saveAlgorithm("Two Pointer", "Maintain left and right pointers moving towards each other to find target sum.", "ARRAY", "O(N)", "O(1)", "MEDIUM",
                "public class ArrayTwoPointer {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {2, 7, 11, 15};\n" +
                "        int target = 9;\n" +
                "        int left = 0;\n" +
                "        int right = arr.length - 1;\n" +
                "        int iterations = 0;\n" +
                "        int comparisons = 0;\n" +
                "        while (left < right) {\n" +
                "            iterations++;\n" +
                "            comparisons++;\n" +
                "            int sum = arr[left] + arr[right];\n" +
                "            if (sum == target) {\n" +
                "                System.out.println(\"Pair found.\");\n" +
                "                break;\n" +
                "            } else if (sum < target) {\n" +
                "                left++;\n" +
                "            } else {\n" +
                "                right--;\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Sliding Window", "Maintain a window of size K and compute metric during window transitions.", "ARRAY", "O(N)", "O(1)", "MEDIUM",
                "public class ArraySlidingWindow {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {2, 1, 5, 1, 3, 2};\n" +
                "        int k = 3;\n" +
                "        int maxSum = 0;\n" +
                "        int windowSum = 0;\n" +
                "        int iterations = 0;\n" +
                "        for (int i = 0; i < k; i++) {\n" +
                "            iterations++;\n" +
                "            windowSum += arr[i];\n" +
                "        }\n" +
                "        maxSum = windowSum;\n" +
                "        for (int i = k; i < arr.length; i++) {\n" +
                "            iterations++;\n" +
                "            windowSum += arr[i] - arr[i - k];\n" +
                "            if (windowSum > maxSum) {\n" +
                "                maxSum = windowSum;\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println(\"Max sum subarray: \" + maxSum);\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Prefix Sum", "Precompute running sums to answer range sum queries in O(1) time.", "ARRAY", "O(N)", "O(N)", "EASY",
                "public class ArrayPrefixSum {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {3, 1, 2, 5, 4};\n" +
                "        int[] prefix = new int[arr.length];\n" +
                "        int iterations = 0;\n" +
                "        prefix[0] = arr[0];\n" +
                "        for (int i = 1; i < arr.length; i++) {\n" +
                "            iterations++;\n" +
                "            prefix[i] = prefix[i - 1] + arr[i];\n" +
                "        }\n" +
                "        System.out.println(\"Prefix sum completed.\");\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Suffix Sum", "Compute cumulative sums starting from the end of the array.", "ARRAY", "O(N)", "O(N)", "EASY",
                "public class ArraySuffixSum {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {3, 1, 2, 5, 4};\n" +
                "        int[] suffix = new int[arr.length];\n" +
                "        int iterations = 0;\n" +
                "        int n = arr.length;\n" +
                "        suffix[n - 1] = arr[n - 1];\n" +
                "        for (int i = n - 2; i >= 0; i--) {\n" +
                "            iterations++;\n" +
                "            suffix[i] = suffix[i + 1] + arr[i];\n" +
                "        }\n" +
                "        System.out.println(\"Suffix sum completed.\");\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Kadane's Algorithm", "Find maximum sum contiguous subarray in linear time.", "ARRAY", "O(N)", "O(1)", "MEDIUM",
                "public class ArrayKadane {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {-2, 1, -3, 4, -1, 2, 1, -5, 4};\n" +
                "        int maxSoFar = arr[0];\n" +
                "        int maxEndingHere = arr[0];\n" +
                "        int iterations = 0;\n" +
                "        int comparisons = 0;\n" +
                "        for (int i = 1; i < arr.length; i++) {\n" +
                "            iterations++;\n" +
                "            maxEndingHere += arr[i];\n" +
                "            comparisons++;\n" +
                "            if (maxEndingHere < arr[i]) {\n" +
                "                maxEndingHere = arr[i];\n" +
                "            }\n" +
                "            comparisons++;\n" +
                "            if (maxSoFar < maxEndingHere) {\n" +
                "                maxSoFar = maxEndingHere;\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println(\"Max subarray sum: \" + maxSoFar);\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Binary Search", "Find target index in a sorted array by repeatedly dividing search space in half.", "ARRAY", "O(log N)", "O(1)", "EASY",
                "public class ArrayBinarySearch {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {2, 5, 8, 12, 16, 23, 38, 56, 72, 91};\n" +
                "        int target = 23;\n" +
                "        int low = 0;\n" +
                "        int high = arr.length - 1;\n" +
                "        int mid = -1;\n" +
                "        int iterations = 0;\n" +
                "        int comparisons = 0;\n" +
                "        while (low <= high) {\n" +
                "            iterations++;\n" +
                "            mid = (low + high) / 2;\n" +
                "            comparisons++;\n" +
                "            if (arr[mid] == target) {\n" +
                "                System.out.println(\"Target found at: \" + mid);\n" +
                "                break;\n" +
                "            }\n" +
                "            comparisons++;\n" +
                "            if (arr[mid] < target) {\n" +
                "                low = mid + 1;\n" +
                "            } else {\n" +
                "                high = mid - 1;\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}");

        log.info("Algorithms seeded successfully.");
    }

    private void saveAlgorithm(String name, String desc, String cat, String tc, String sc, String diff, String code) {
        Algorithm algo = new Algorithm();
        algo.setName(name);
        algo.setDescription(desc);
        algo.setCategory(cat);
        algo.setTimeComplexity(tc);
        algo.setSpaceComplexity(sc);
        algo.setDifficultyLevel(diff);
        algo.setJavaCode(code);
        algorithmRepository.save(algo);
    }

    private void seedPracticeProblems() {
        log.info("Seeding practice problems...");

        saveProblem("Find the Maximum Element",
                "Write a program to find the maximum element in a given array of integers.",
                "EASY", "ARRAY",
                "public class FindMax {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {12, 34, 45, 9, 8, 90, 3};\n" +
                "        int max = arr[0];\n" +
                "        // TODO: Write code to find max element\n\n" +
                "        System.out.println(\"Max element: \" + max);\n" +
                "    }\n" +
                "}",
                "public class FindMax {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {12, 34, 45, 9, 8, 90, 3};\n" +
                "        int max = arr[0];\n" +
                "        for (int i = 1; i < arr.length; i++) {\n" +
                "            if (arr[i] > max) {\n" +
                "                max = arr[i];\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println(\"Max element: \" + max);\n" +
                "    }\n" +
                "}",
                "Max element: 90",
                "FindMax");

        saveProblem("Reverse the Array",
                "Write a program to reverse the elements of a given array of integers and print the reversed array separated by spaces.",
                "EASY", "ARRAY",
                "public class ReverseArray {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {1, 2, 3, 4, 5};\n" +
                "        // TODO: Reverse the array\n\n" +
                "        for (int x : arr) {\n" +
                "            System.out.print(x + \" \");\n" +
                "        }\n" +
                "        System.out.println();\n" +
                "    }\n" +
                "}",
                "public class ReverseArray {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {1, 2, 3, 4, 5};\n" +
                "        int n = arr.length;\n" +
                "        for (int i = 0; i < n / 2; i++) {\n" +
                "            int temp = arr[i];\n" +
                "            arr[i] = arr[n - 1 - i];\n" +
                "            arr[n - 1 - i] = temp;\n" +
                "        }\n" +
                "        for (int x : arr) {\n" +
                "            System.out.print(x + \" \");\n" +
                "        }\n" +
                "        System.out.println();\n" +
                "    }\n" +
                "}",
                "5 4 3 2 1",
                "ReverseArray");

        saveProblem("Two Sum",
                "Given a sorted array of integers, write a program to find if a pair of elements exists that sums to target = 9.",
                "MEDIUM", "ARRAY",
                "public class TwoSum {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {2, 7, 11, 15};\n" +
                "        int target = 9;\n" +
                "        boolean found = false;\n" +
                "        // TODO: Implement Two Pointer target sum check\n\n" +
                "        System.out.println(\"Pair found: \" + found);\n" +
                "    }\n" +
                "}",
                "public class TwoSum {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {2, 7, 11, 15};\n" +
                "        int target = 9;\n" +
                "        boolean found = false;\n" +
                "        int left = 0;\n" +
                "        int right = arr.length - 1;\n" +
                "        while (left < right) {\n" +
                "            int sum = arr[left] + arr[right];\n" +
                "            if (sum == target) {\n" +
                "                found = true;\n" +
                "                break;\n" +
                "            } else if (sum < target) {\n" +
                "                left++;\n" +
                "            } else {\n" +
                "                right--;\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println(\"Pair found: \" + found);\n" +
                "    }\n" +
                "}",
                "Pair found: true",
                "TwoSum");

        saveProblem("Maximum Subarray Sum",
                "Find the contiguous subarray within a one-dimensional array of numbers which has the largest sum.",
                "MEDIUM", "ARRAY",
                "public class MaxSubarray {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {-2, 1, -3, 4, -1, 2, 1, -5, 4};\n" +
                "        int maxSum = arr[0];\n" +
                "        // TODO: Write Kadane's algorithm\n\n" +
                "        System.out.println(\"Max Sum: \" + maxSum);\n" +
                "    }\n" +
                "}",
                "public class MaxSubarray {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {-2, 1, -3, 4, -1, 2, 1, -5, 4};\n" +
                "        int maxSum = arr[0];\n" +
                "        int currentSum = arr[0];\n" +
                "        for (int i = 1; i < arr.length; i++) {\n" +
                "            currentSum = Math.max(arr[i], currentSum + arr[i]);\n" +
                "            maxSum = Math.max(maxSum, currentSum);\n" +
                "        }\n" +
                "        System.out.println(\"Max Sum: \" + maxSum);\n" +
                "    }\n" +
                "}",
                "Max Sum: 6",
                "MaxSubarray");

        saveProblem("Trapping Rain Water",
                "Compute how much water an elevation map can trap after raining.",
                "HARD", "ARRAY",
                "public class TrappingWater {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] height = {0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1};\n" +
                "        int trapped = 0;\n" +
                "        // TODO: Calculate trapped water using two pointers\n\n" +
                "        System.out.println(\"Trapped water: \" + trapped);\n" +
                "    }\n" +
                "}",
                "public class TrappingWater {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] height = {0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1};\n" +
                "        int left = 0, right = height.length - 1;\n" +
                "        int leftMax = 0, rightMax = 0;\n" +
                "        int trapped = 0;\n" +
                "        while (left < right) {\n" +
                "            if (height[left] < height[right]) {\n" +
                "                if (height[left] >= leftMax) {\n" +
                "                    leftMax = height[left];\n" +
                "                } else {\n" +
                "                    trapped += leftMax - height[left];\n" +
                "                }\n" +
                "                left++;\n" +
                "            } else {\n" +
                "                if (height[right] >= rightMax) {\n" +
                "                    rightMax = height[right];\n" +
                "                } else {\n" +
                "                    trapped += rightMax - height[right];\n" +
                "                }\n" +
                "                right--;\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println(\"Trapped water: \" + trapped);\n" +
                "    }\n" +
                "}",
                "Trapped water: 6",
                "TrappingWater");

        saveProblem("Sliding Window Maximum",
                "Print the maximum element in each sliding window of size k = 3.",
                "HARD", "ARRAY",
                "public class WindowMax {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {1, 3, -1, -3, 5, 3, 6, 7};\n" +
                "        int k = 3;\n" +
                "        // TODO: Print window maximums separated by spaces\n\n" +
                "    }\n" +
                "}",
                "public class WindowMax {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {1, 3, -1, -3, 5, 3, 6, 7};\n" +
                "        int k = 3;\n" +
                "        for (int i = 0; i <= arr.length - k; i++) {\n" +
                "            int max = arr[i];\n" +
                "            for (int j = 1; j < k; j++) {\n" +
                "                if (arr[i + j] > max) {\n" +
                "                    max = arr[i + j];\n" +
                "                }\n" +
                "            }\n" +
                "            System.out.print(max + \" \");\n" +
                "        }\n" +
                "        System.out.println();\n" +
                "    }\n" +
                "}",
                "3 3 5 5 6 7",
                "WindowMax");

        log.info("Practice problems seeded successfully.");
    }

    private void seedStringAlgorithms() {
        log.info("Seeding String algorithms...");

        saveAlgorithm("Traversal", "Iterate through each character of a string.", "STRING", "O(N)", "O(1)", "EASY",
                "public class StringTraversal {\n" +
                "    public static void main(String[] args) {\n" +
                "        String str = \"Hello World\";\n" +
                "        int iterations = 0;\n" +
                "        for (int i = 0; i < str.length(); i++) {\n" +
                "            iterations++;\n" +
                "            char c = str.charAt(i);\n" +
                "            System.out.println(\"Character at \" + i + \": \" + c);\n" +
                "        }\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Reverse String", "Reverse a string using two pointers.", "STRING", "O(N)", "O(N)", "EASY",
                "public class StringReverse {\n" +
                "    public static void main(String[] args) {\n" +
                "        String str = \"Antigravity\";\n" +
                "        char[] chars = str.toCharArray();\n" +
                "        int left = 0;\n" +
                "        int right = chars.length - 1;\n" +
                "        while (left < right) {\n" +
                "            char temp = chars[left];\n" +
                "            chars[left] = chars[right];\n" +
                "            chars[right] = temp;\n" +
                "            left++;\n" +
                "            right--;\n" +
                "        }\n" +
                "        System.out.println(new String(chars));\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Palindrome", "Check if a string is a palindrome using two pointers.", "STRING", "O(N)", "O(1)", "EASY",
                "public class StringPalindrome {\n" +
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
                "}");

        saveAlgorithm("Character Frequency", "Count character occurrences in a string.", "STRING", "O(N)", "O(1)", "EASY",
                "public class StringCharFreq {\n" +
                "    public static void main(String[] args) {\n" +
                "        String str = \"success\";\n" +
                "        int[] freq = new int[256];\n" +
                "        for (int i = 0; i < str.length(); i++) {\n" +
                "            freq[str.charAt(i)]++;\n" +
                "        }\n" +
                "        System.out.println(\"Frequency of s: \" + freq['s']);\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Anagram", "Check if two strings are anagrams.", "STRING", "O(N)", "O(1)", "EASY",
                "public class StringAnagram {\n" +
                "    public static void main(String[] args) {\n" +
                "        String s1 = \"listen\";\n" +
                "        String s2 = \"silent\";\n" +
                "        boolean isAnagram = true;\n" +
                "        if (s1.length() != s2.length()) {\n" +
                "            isAnagram = false;\n" +
                "        } else {\n" +
                "            int[] counts = new int[256];\n" +
                "            for (int i = 0; i < s1.length(); i++) {\n" +
                "                counts[s1.charAt(i)]++;\n" +
                "                counts[s2.charAt(i)]--;\n" +
                "            }\n" +
                "            for (int c : counts) {\n" +
                "                if (c != 0) { isAnagram = false; break; }\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println(\"Is Anagram: \" + isAnagram);\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Naive Pattern Matching", "Brute-force pattern search.", "STRING", "O(N*M)", "O(1)", "EASY",
                "public class StringNaiveMatch {\n" +
                "    public static void main(String[] args) {\n" +
                "        String text = \"AABAACAADAABAAABDF\";\n" +
                "        String pattern = \"AABA\";\n" +
                "        int n = text.length();\n" +
                "        int m = pattern.length();\n" +
                "        for (int i = 0; i <= n - m; i++) {\n" +
                "            int j;\n" +
                "            for (j = 0; j < m; j++) {\n" +
                "                if (text.charAt(i + j) != pattern.charAt(j)) break;\n" +
                "            }\n" +
                "            if (j == m) System.out.println(\"Pattern found at: \" + i);\n" +
                "        }\n" +
                "    }\n" +
                "}");

        saveAlgorithm("KMP Search", "Knuth-Morris-Pratt search.", "STRING", "O(N+M)", "O(M)", "MEDIUM",
                "public class StringKmp {\n" +
                "    public static void main(String[] args) {\n" +
                "        String text = \"ABABDABACDABABCABAB\";\n" +
                "        String pattern = \"ABABCABAB\";\n" +
                "        int[] lps = {0, 0, 1, 2, 0, 1, 2, 3, 4};\n" +
                "        int i = 0, j = 0;\n" +
                "        while (i < text.length()) {\n" +
                "            if (pattern.charAt(j) == text.charAt(i)) {\n" +
                "                i++; j++;\n" +
                "            }\n" +
                "            if (j == pattern.length()) {\n" +
                "                System.out.println(\"Found pattern at: \" + (i - j));\n" +
                "                j = lps[j - 1];\n" +
                "            } else if (i < text.length() && pattern.charAt(j) != text.charAt(i)) {\n" +
                "                if (j != 0) j = lps[j - 1];\n" +
                "                else i++;\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}");
    }

    private void seedLinkedListAlgorithms() {
        log.info("Seeding Linked List algorithms...");

        saveAlgorithm("Singly Linked List", "Create and traverse a Singly Linked List.", "LINKED_LIST", "O(N)", "O(N)", "EASY",
                "public class SinglyLinkedList {\n" +
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
                "        while (current != null) {\n" +
                "            System.out.print(current.data + \" -> \");\n" +
                "            current = current.next;\n" +
                "        }\n" +
                "        System.out.println(\"null\");\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Doubly Linked List", "Create and traverse a Doubly Linked List.", "LINKED_LIST", "O(N)", "O(N)", "EASY",
                "public class DoublyLinkedList {\n" +
                "    static class Node {\n" +
                "        int data;\n" +
                "        Node next, prev;\n" +
                "        Node(int data) { this.data = data; }\n" +
                "    }\n" +
                "    public static void main(String[] args) {\n" +
                "        Node head = new Node(10);\n" +
                "        Node second = new Node(20);\n" +
                "        head.next = second;\n" +
                "        second.prev = head;\n" +
                "        Node current = head;\n" +
                "        while (current != null) {\n" +
                "            System.out.println(\"Node: \" + current.data);\n" +
                "            current = current.next;\n" +
                "        }\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Insert at Beginning", "Insert a node at the start of a Linked List.", "LINKED_LIST", "O(1)", "O(1)", "EASY",
                "public class LinkedListInsertBegin {\n" +
                "    static class Node {\n" +
                "        int data;\n" +
                "        Node next;\n" +
                "        Node(int data) { this.data = data; }\n" +
                "    }\n" +
                "    public static void main(String[] args) {\n" +
                "        Node head = new Node(20);\n" +
                "        head.next = new Node(30);\n" +
                "        Node newNode = new Node(10);\n" +
                "        newNode.next = head;\n" +
                "        head = newNode;\n" +
                "        System.out.println(\"Head value: \" + head.data);\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Delete Node", "Delete a node from a Linked List.", "LINKED_LIST", "O(N)", "O(1)", "EASY",
                "public class LinkedListDelete {\n" +
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
                "        if (current.next != null && current.next.data == 20) {\n" +
                "            current.next = current.next.next;\n" +
                "        }\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Reverse List", "Reverse a Singly Linked List iteratively.", "LINKED_LIST", "O(N)", "O(1)", "MEDIUM",
                "public class LinkedListReverse {\n" +
                "    static class Node {\n" +
                "        int data;\n" +
                "        Node next;\n" +
                "        Node(int data) { this.data = data; }\n" +
                "    }\n" +
                "    public static void main(String[] args) {\n" +
                "        Node head = new Node(1);\n" +
                "        head.next = new Node(2);\n" +
                "        head.next.next = new Node(3);\n" +
                "        Node prev = null;\n" +
                "        Node curr = head;\n" +
                "        while (curr != null) {\n" +
                "            Node next = curr.next;\n" +
                "            curr.next = prev;\n" +
                "            prev = curr;\n" +
                "            curr = next;\n" +
                "        }\n" +
                "        head = prev;\n" +
                "        System.out.println(\"Reversed Head: \" + head.data);\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Detect Cycle", "Detect cycle using Floyd's Tortoise and Hare.", "LINKED_LIST", "O(N)", "O(1)", "MEDIUM",
                "public class LinkedListCycle {\n" +
                "    static class Node {\n" +
                "        int data;\n" +
                "        Node next;\n" +
                "        Node(int data) { this.data = data; }\n" +
                "    }\n" +
                "    public static void main(String[] args) {\n" +
                "        Node head = new Node(1);\n" +
                "        head.next = new Node(2);\n" +
                "        head.next.next = new Node(3);\n" +
                "        head.next.next.next = head.next; // Create cycle\n" +
                "        Node slow = head;\n" +
                "        Node fast = head;\n" +
                "        boolean hasCycle = false;\n" +
                "        while (fast != null && fast.next != null) {\n" +
                "            slow = slow.next;\n" +
                "            fast = fast.next.next;\n" +
                "            if (slow == fast) {\n" +
                "                hasCycle = true;\n" +
                "                break;\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println(\"Has Cycle: \" + hasCycle);\n" +
                "    }\n" +
                "}");
    }

    private void seedStringPracticeProblems() {
        log.info("Seeding String practice problems...");

        saveProblem("Reverse a String",
                "Write a program to reverse a given string and print the result.",
                "EASY", "STRING",
                "public class PracticeStringReverse {\n" +
                "    public static void main(String[] args) {\n" +
                "        String str = \"hello\";\n" +
                "        // TODO: Reverse the string\n\n" +
                "    }\n" +
                "}",
                "public class PracticeStringReverse {\n" +
                "    public static void main(String[] args) {\n" +
                "        String str = \"hello\";\n" +
                "        StringBuilder sb = new StringBuilder(str);\n" +
                "        System.out.println(sb.reverse().toString());\n" +
                "    }\n" +
                "}",
                "olleh",
                "PracticeStringReverse");

        saveProblem("Valid Anagram Check",
                "Write a program to check if two strings are valid anagrams of each other (print true or false).",
                "EASY", "STRING",
                "public class PracticeAnagram {\n" +
                "    public static void main(String[] args) {\n" +
                "        String s1 = \"anagram\";\n" +
                "        String s2 = \"nagaram\";\n" +
                "        boolean result = false;\n" +
                "        // TODO: Check if s1 and s2 are anagrams\n\n" +
                "        System.out.println(result);\n" +
                "    }\n" +
                "}",
                "public class PracticeAnagram {\n" +
                "    public static void main(String[] args) {\n" +
                "        String s1 = \"anagram\";\n" +
                "        String s2 = \"nagaram\";\n" +
                "        boolean result = true;\n" +
                "        if (s1.length() != s2.length()) {\n" +
                "            result = false;\n" +
                "        } else {\n" +
                "            int[] counts = new int[256];\n" +
                "            for (int i = 0; i < s1.length(); i++) {\n" +
                "                counts[s1.charAt(i)]++;\n" +
                "                counts[s2.charAt(i)]--;\n" +
                "            }\n" +
                "            for (int c : counts) {\n" +
                "                if (c != 0) { result = false; break; }\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println(result);\n" +
                "    }\n" +
                "}",
                "true",
                "PracticeAnagram");

        saveProblem("Valid Palindrome Check",
                "Write a program to check if a string is a palindrome (ignoring non-alphanumeric chars and case).",
                "MEDIUM", "STRING",
                "public class PracticePalindrome {\n" +
                "    public static void main(String[] args) {\n" +
                "        String str = \"A man, a plan, a canal: Panama\";\n" +
                "        boolean result = false;\n" +
                "        // TODO: Validate palindrome\n\n" +
                "        System.out.println(result);\n" +
                "    }\n" +
                "}",
                "public class PracticePalindrome {\n" +
                "    public static void main(String[] args) {\n" +
                "        String str = \"A man, a plan, a canal: Panama\";\n" +
                "        String clean = str.replaceAll(\"[^a-zA-Z0-9]\", \"\").toLowerCase();\n" +
                "        boolean result = true;\n" +
                "        int left = 0, right = clean.length() - 1;\n" +
                "        while (left < right) {\n" +
                "            if (clean.charAt(left) != clean.charAt(right)) {\n" +
                "                result = false;\n" +
                "                break;\n" +
                "            }\n" +
                "            left++; right--;\n" +
                "        }\n" +
                "        System.out.println(result);\n" +
                "    }\n" +
                "}",
                "true",
                "PracticePalindrome");
    }

    private void seedLinkedListPracticeProblems() {
        log.info("Seeding LinkedList practice problems...");

        saveProblem("Length of Linked List",
                "Write a program to calculate the length of a linked list.",
                "EASY", "LINKED_LIST",
                "public class PracticeListLength {\n" +
                "    static class Node {\n" +
                "        int data;\n" +
                "        Node next;\n" +
                "        Node(int data) { this.data = data; }\n" +
                "    }\n" +
                "    public static void main(String[] args) {\n" +
                "        Node head = new Node(10);\n" +
                "        head.next = new Node(20);\n" +
                "        head.next.next = new Node(30);\n" +
                "        int length = 0;\n" +
                "        // TODO: Calculate length\n\n" +
                "        System.out.println(length);\n" +
                "    }\n" +
                "}",
                "public class PracticeListLength {\n" +
                "    static class Node {\n" +
                "        int data;\n" +
                "        Node next;\n" +
                "        Node(int data) { this.data = data; }\n" +
                "    }\n" +
                "    public static void main(String[] args) {\n" +
                "        Node head = new Node(10);\n" +
                "        head.next = new Node(20);\n" +
                "        head.next.next = new Node(30);\n" +
                "        int length = 0;\n" +
                "        Node current = head;\n" +
                "        while (current != null) {\n" +
                "            length++;\n" +
                "            current = current.next;\n" +
                "        }\n" +
                "        System.out.println(length);\n" +
                "    }\n" +
                "}",
                "3",
                "PracticeListLength");

        saveProblem("Middle of LinkedList",
                "Write a program to find the data value of the middle node of a linked list (for even size, second mid).",
                "EASY", "LINKED_LIST",
                "public class PracticeListMiddle {\n" +
                "    static class Node {\n" +
                "        int data;\n" +
                "        Node next;\n" +
                "        Node(int data) { this.data = data; }\n" +
                "    }\n" +
                "    public static void main(String[] args) {\n" +
                "        Node head = new Node(1);\n" +
                "        head.next = new Node(2);\n" +
                "        head.next.next = new Node(3);\n" +
                "        head.next.next.next = new Node(4);\n" +
                "        int middleVal = -1;\n" +
                "        // TODO: Find middle node data\n\n" +
                "        System.out.println(middleVal);\n" +
                "    }\n" +
                "}",
                "public class PracticeListMiddle {\n" +
                "    static class Node {\n" +
                "        int data;\n" +
                "        Node next;\n" +
                "        Node(int data) { this.data = data; }\n" +
                "    }\n" +
                "    public static void main(String[] args) {\n" +
                "        Node head = new Node(1);\n" +
                "        head.next = new Node(2);\n" +
                "        head.next.next = new Node(3);\n" +
                "        head.next.next.next = new Node(4);\n" +
                "        Node slow = head;\n" +
                "        Node fast = head;\n" +
                "        while (fast != null && fast.next != null) {\n" +
                "            slow = slow.next;\n" +
                "            fast = fast.next.next;\n" +
                "        }\n" +
                "        System.out.println(slow.data);\n" +
                "    }\n" +
                "}",
                "3",
                "PracticeListMiddle");

        saveProblem("Detect Loop in LinkedList",
                "Write a program to check if a linked list contains a cycle (print true or false).",
                "MEDIUM", "LINKED_LIST",
                "public class PracticeListLoop {\n" +
                "    static class Node {\n" +
                "        int data;\n" +
                "        Node next;\n" +
                "        Node(int data) { this.data = data; }\n" +
                "    }\n" +
                "    public static void main(String[] args) {\n" +
                "        Node head = new Node(1);\n" +
                "        head.next = new Node(2);\n" +
                "        head.next.next = head; // Loop\n" +
                "        boolean result = false;\n" +
                "        // TODO: Detect cycle\n\n" +
                "        System.out.println(result);\n" +
                "    }\n" +
                "}",
                "public class PracticeListLoop {\n" +
                "    static class Node {\n" +
                "        int data;\n" +
                "        Node next;\n" +
                "        Node(int data) { this.data = data; }\n" +
                "    }\n" +
                "    public static void main(String[] args) {\n" +
                "        Node head = new Node(1);\n" +
                "        head.next = new Node(2);\n" +
                "        head.next.next = head;\n" +
                "        Node slow = head;\n" +
                "        Node fast = head;\n" +
                "        boolean hasCycle = false;\n" +
                "        while (fast != null && fast.next != null) {\n" +
                "            slow = slow.next;\n" +
                "            fast = fast.next.next;\n" +
                "            if (slow == fast) {\n" +
                "                hasCycle = true;\n" +
                "                break;\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println(hasCycle);\n" +
                "    }\n" +
                "}",
                "true",
                "PracticeListLoop");
    }

    private void saveProblem(String title, String desc, String diff, String cat, String starter, String soln, String output, String className) {
        PracticeProblem problem = new PracticeProblem();
        problem.setTitle(title);
        problem.setDescription(desc);
        problem.setDifficulty(diff);
        problem.setCategory(cat);
        problem.setStarterCode(starter);
        problem.setSolutionCode(soln);
        problem.setExpectedOutput(output);
        problem.setClassName(className);
        practiceProblemRepository.save(problem);
    }

    // ══════════════════════════════════════════════════════════════════
    // STACK ALGORITHMS
    // ══════════════════════════════════════════════════════════════════
    private void seedStackAlgorithms() {
        log.info("Seeding Stack algorithms...");

        saveAlgorithm("Stack using Array", "Implement a stack using a fixed-size array with top pointer.", "STACK", "O(1) push/pop", "O(N)", "EASY",
                "public class StackArray {\n" +
                "    static int[] arr = new int[5];\n" +
                "    static int top = -1;\n" +
                "    static void push(int x) { if (top < arr.length - 1) arr[++top] = x; }\n" +
                "    static int pop() { if (top >= 0) return arr[top--]; return -1; }\n" +
                "    static int peek() { return top >= 0 ? arr[top] : -1; }\n" +
                "    public static void main(String[] args) {\n" +
                "        push(10);\n" +
                "        push(20);\n" +
                "        push(30);\n" +
                "        System.out.println(\"Peek: \" + peek());\n" +
                "        System.out.println(\"Pop: \" + pop());\n" +
                "        System.out.println(\"Pop: \" + pop());\n" +
                "        System.out.println(\"Peek: \" + peek());\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Java Stack", "Use java.util.Stack to push, pop and peek elements.", "STACK", "O(1) amortized", "O(N)", "EASY",
                "import java.util.Stack;\n" +
                "public class JavaStack {\n" +
                "    public static void main(String[] args) {\n" +
                "        Stack<Integer> stack = new Stack<>();\n" +
                "        stack.push(10);\n" +
                "        stack.push(20);\n" +
                "        stack.push(30);\n" +
                "        System.out.println(\"Peek: \" + stack.peek());\n" +
                "        System.out.println(\"Pop: \" + stack.pop());\n" +
                "        System.out.println(\"Size: \" + stack.size());\n" +
                "        System.out.println(\"Empty: \" + stack.isEmpty());\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Deque as Stack", "Use ArrayDeque as a stack (preferred over java.util.Stack).", "STACK", "O(1)", "O(N)", "EASY",
                "import java.util.ArrayDeque;\n" +
                "import java.util.Deque;\n" +
                "public class DequeStack {\n" +
                "    public static void main(String[] args) {\n" +
                "        Deque<Integer> stack = new ArrayDeque<>();\n" +
                "        stack.push(10);\n" +
                "        stack.push(20);\n" +
                "        stack.push(30);\n" +
                "        System.out.println(\"Peek: \" + stack.peek());\n" +
                "        System.out.println(\"Pop: \" + stack.pop());\n" +
                "        System.out.println(\"Size: \" + stack.size());\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Balanced Parentheses", "Check if parentheses, brackets, and braces are balanced using a stack.", "STACK", "O(N)", "O(N)", "EASY",
                "import java.util.Stack;\n" +
                "public class BalancedParentheses {\n" +
                "    public static void main(String[] args) {\n" +
                "        String s = \"({[]})\";\n" +
                "        Stack<Character> stack = new Stack<>();\n" +
                "        boolean valid = true;\n" +
                "        for (int i = 0; i < s.length(); i++) {\n" +
                "            char c = s.charAt(i);\n" +
                "            if (c == '(' || c == '{' || c == '[') {\n" +
                "                stack.push(c);\n" +
                "            } else {\n" +
                "                if (stack.isEmpty()) { valid = false; break; }\n" +
                "                char top = stack.pop();\n" +
                "                if ((c == ')' && top != '(') ||\n" +
                "                    (c == '}' && top != '{') ||\n" +
                "                    (c == ']' && top != '[')) {\n" +
                "                    valid = false; break;\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "        if (!stack.isEmpty()) valid = false;\n" +
                "        System.out.println(\"Valid: \" + valid);\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Next Greater Element", "Find the Next Greater Element for each element using a monotonic stack.", "STACK", "O(N)", "O(N)", "MEDIUM",
                "import java.util.Stack;\n" +
                "import java.util.Arrays;\n" +
                "public class NextGreaterElement {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {4, 5, 2, 10, 8};\n" +
                "        int n = arr.length;\n" +
                "        int[] nge = new int[n];\n" +
                "        Arrays.fill(nge, -1);\n" +
                "        Stack<Integer> stack = new Stack<>();\n" +
                "        for (int i = 0; i < n; i++) {\n" +
                "            while (!stack.isEmpty() && arr[stack.peek()] < arr[i]) {\n" +
                "                nge[stack.pop()] = arr[i];\n" +
                "            }\n" +
                "            stack.push(i);\n" +
                "        }\n" +
                "        System.out.println(Arrays.toString(nge));\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Previous Greater Element", "Find the Previous Greater Element for each element using a monotonic stack.", "STACK", "O(N)", "O(N)", "MEDIUM",
                "import java.util.Stack;\n" +
                "import java.util.Arrays;\n" +
                "public class PreviousGreaterElement {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {4, 5, 2, 10, 8};\n" +
                "        int n = arr.length;\n" +
                "        int[] pge = new int[n];\n" +
                "        Arrays.fill(pge, -1);\n" +
                "        Stack<Integer> stack = new Stack<>();\n" +
                "        for (int i = 0; i < n; i++) {\n" +
                "            while (!stack.isEmpty() && stack.peek() <= arr[i]) {\n" +
                "                stack.pop();\n" +
                "            }\n" +
                "            pge[i] = stack.isEmpty() ? -1 : stack.peek();\n" +
                "            stack.push(arr[i]);\n" +
                "        }\n" +
                "        System.out.println(Arrays.toString(pge));\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Next Smaller Element", "Find the Next Smaller Element for each element using a monotonic stack.", "STACK", "O(N)", "O(N)", "MEDIUM",
                "import java.util.Stack;\n" +
                "import java.util.Arrays;\n" +
                "public class NextSmallerElement {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {4, 5, 2, 10, 8};\n" +
                "        int n = arr.length;\n" +
                "        int[] nse = new int[n];\n" +
                "        Arrays.fill(nse, -1);\n" +
                "        Stack<Integer> stack = new Stack<>();\n" +
                "        for (int i = n - 1; i >= 0; i--) {\n" +
                "            while (!stack.isEmpty() && stack.peek() >= arr[i]) {\n" +
                "                stack.pop();\n" +
                "            }\n" +
                "            nse[i] = stack.isEmpty() ? -1 : stack.peek();\n" +
                "            stack.push(arr[i]);\n" +
                "        }\n" +
                "        System.out.println(Arrays.toString(nse));\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Infix to Postfix", "Convert an infix expression to postfix notation using a stack.", "STACK", "O(N)", "O(N)", "MEDIUM",
                "import java.util.Stack;\n" +
                "public class InfixToPostfix {\n" +
                "    static int precedence(char op) {\n" +
                "        if (op == '+' || op == '-') return 1;\n" +
                "        if (op == '*' || op == '/') return 2;\n" +
                "        return 0;\n" +
                "    }\n" +
                "    public static void main(String[] args) {\n" +
                "        String infix = \"a+b*c-d\";\n" +
                "        Stack<Character> stack = new Stack<>();\n" +
                "        StringBuilder postfix = new StringBuilder();\n" +
                "        for (int i = 0; i < infix.length(); i++) {\n" +
                "            char c = infix.charAt(i);\n" +
                "            if (Character.isLetterOrDigit(c)) {\n" +
                "                postfix.append(c);\n" +
                "            } else if (c == '(') {\n" +
                "                stack.push(c);\n" +
                "            } else if (c == ')') {\n" +
                "                while (!stack.isEmpty() && stack.peek() != '(') {\n" +
                "                    postfix.append(stack.pop());\n" +
                "                }\n" +
                "                stack.pop();\n" +
                "            } else {\n" +
                "                while (!stack.isEmpty() && precedence(stack.peek()) >= precedence(c)) {\n" +
                "                    postfix.append(stack.pop());\n" +
                "                }\n" +
                "                stack.push(c);\n" +
                "            }\n" +
                "        }\n" +
                "        while (!stack.isEmpty()) postfix.append(stack.pop());\n" +
                "        System.out.println(postfix);\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Postfix Evaluation", "Evaluate a postfix expression using a stack.", "STACK", "O(N)", "O(N)", "MEDIUM",
                "import java.util.Stack;\n" +
                "public class PostfixEval {\n" +
                "    public static void main(String[] args) {\n" +
                "        String expr = \"231*+9-\";\n" +
                "        Stack<Integer> stack = new Stack<>();\n" +
                "        for (int i = 0; i < expr.length(); i++) {\n" +
                "            char c = expr.charAt(i);\n" +
                "            if (Character.isDigit(c)) {\n" +
                "                stack.push(c - '0');\n" +
                "            } else {\n" +
                "                int b = stack.pop();\n" +
                "                int a = stack.pop();\n" +
                "                if (c == '+') stack.push(a + b);\n" +
                "                else if (c == '-') stack.push(a - b);\n" +
                "                else if (c == '*') stack.push(a * b);\n" +
                "                else stack.push(a / b);\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println(\"Result: \" + stack.pop());\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Min Stack", "Design a stack that supports O(1) getMin() using an auxiliary stack.", "STACK", "O(1) all ops", "O(N)", "MEDIUM",
                "import java.util.Stack;\n" +
                "public class MinStack {\n" +
                "    Stack<Integer> stack = new Stack<>();\n" +
                "    Stack<Integer> minSt = new Stack<>();\n" +
                "    void push(int x) {\n" +
                "        stack.push(x);\n" +
                "        if (minSt.isEmpty() || x <= minSt.peek()) minSt.push(x);\n" +
                "    }\n" +
                "    int pop() {\n" +
                "        int val = stack.pop();\n" +
                "        if (val == minSt.peek()) minSt.pop();\n" +
                "        return val;\n" +
                "    }\n" +
                "    int getMin() { return minSt.peek(); }\n" +
                "    public static void main(String[] args) {\n" +
                "        MinStack ms = new MinStack();\n" +
                "        ms.push(5);\n" +
                "        ms.push(3);\n" +
                "        ms.push(7);\n" +
                "        ms.push(1);\n" +
                "        System.out.println(\"Min: \" + ms.getMin());\n" +
                "        ms.pop();\n" +
                "        System.out.println(\"Min: \" + ms.getMin());\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Reverse Stack", "Reverse a stack using only push/pop operations (no extra array).", "STACK", "O(N²)", "O(N)", "MEDIUM",
                "import java.util.Stack;\n" +
                "public class StackReverse {\n" +
                "    static void insertAtBottom(Stack<Integer> st, int x) {\n" +
                "        if (st.isEmpty()) { st.push(x); return; }\n" +
                "        int top = st.pop();\n" +
                "        insertAtBottom(st, x);\n" +
                "        st.push(top);\n" +
                "    }\n" +
                "    static void reverse(Stack<Integer> st) {\n" +
                "        if (st.isEmpty()) return;\n" +
                "        int top = st.pop();\n" +
                "        reverse(st);\n" +
                "        insertAtBottom(st, top);\n" +
                "    }\n" +
                "    public static void main(String[] args) {\n" +
                "        Stack<Integer> st = new Stack<>();\n" +
                "        st.push(1); st.push(2); st.push(3);\n" +
                "        reverse(st);\n" +
                "        System.out.println(st);\n" +
                "    }\n" +
                "}");

        log.info("Stack algorithms seeded.");
    }

    // ══════════════════════════════════════════════════════════════════
    // QUEUE ALGORITHMS
    // ══════════════════════════════════════════════════════════════════
    private void seedQueueAlgorithms() {
        log.info("Seeding Queue algorithms...");

        saveAlgorithm("Queue using Array", "Implement a FIFO queue using a fixed-size array with front and rear pointers.", "QUEUE", "O(1) enqueue/dequeue", "O(N)", "EASY",
                "public class QueueArray {\n" +
                "    static int[] arr = new int[5];\n" +
                "    static int front = 0, rear = -1, size = 0;\n" +
                "    static void enqueue(int x) { if (size < arr.length) { arr[++rear] = x; size++; } }\n" +
                "    static int dequeue() { if (size > 0) { size--; return arr[front++]; } return -1; }\n" +
                "    static int peek() { return size > 0 ? arr[front] : -1; }\n" +
                "    public static void main(String[] args) {\n" +
                "        enqueue(10);\n" +
                "        enqueue(20);\n" +
                "        enqueue(30);\n" +
                "        System.out.println(\"Peek: \" + peek());\n" +
                "        System.out.println(\"Dequeue: \" + dequeue());\n" +
                "        System.out.println(\"Dequeue: \" + dequeue());\n" +
                "        System.out.println(\"Peek: \" + peek());\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Java Queue (LinkedList)", "Use java.util.LinkedList as a Queue.", "QUEUE", "O(1)", "O(N)", "EASY",
                "import java.util.Queue;\n" +
                "import java.util.LinkedList;\n" +
                "public class JavaQueue {\n" +
                "    public static void main(String[] args) {\n" +
                "        Queue<Integer> queue = new LinkedList<>();\n" +
                "        queue.offer(10);\n" +
                "        queue.offer(20);\n" +
                "        queue.offer(30);\n" +
                "        System.out.println(\"Peek: \" + queue.peek());\n" +
                "        System.out.println(\"Poll: \" + queue.poll());\n" +
                "        System.out.println(\"Size: \" + queue.size());\n" +
                "        System.out.println(\"Empty: \" + queue.isEmpty());\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Circular Queue", "Implement a Circular Queue using array with wrap-around using modulus.", "QUEUE", "O(1)", "O(N)", "MEDIUM",
                "public class CircularQueue {\n" +
                "    int[] arr;\n" +
                "    int front = 0, rear = 0, size = 0;\n" +
                "    CircularQueue(int capacity) { arr = new int[capacity]; }\n" +
                "    void enqueue(int x) {\n" +
                "        if (size == arr.length) { System.out.println(\"Queue full\"); return; }\n" +
                "        arr[rear] = x;\n" +
                "        rear = (rear + 1) % arr.length;\n" +
                "        size++;\n" +
                "    }\n" +
                "    int dequeue() {\n" +
                "        if (size == 0) return -1;\n" +
                "        int val = arr[front];\n" +
                "        front = (front + 1) % arr.length;\n" +
                "        size--;\n" +
                "        return val;\n" +
                "    }\n" +
                "    public static void main(String[] args) {\n" +
                "        CircularQueue cq = new CircularQueue(4);\n" +
                "        cq.enqueue(10); cq.enqueue(20); cq.enqueue(30);\n" +
                "        System.out.println(cq.dequeue());\n" +
                "        cq.enqueue(40); cq.enqueue(50);\n" +
                "        System.out.println(cq.dequeue());\n" +
                "        System.out.println(cq.size);\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Priority Queue", "Use java.util.PriorityQueue (min-heap) to process elements in priority order.", "QUEUE", "O(log N) offer/poll", "O(N)", "MEDIUM",
                "import java.util.PriorityQueue;\n" +
                "public class PriorityQueueDemo {\n" +
                "    public static void main(String[] args) {\n" +
                "        PriorityQueue<Integer> pq = new PriorityQueue<>();\n" +
                "        pq.offer(40);\n" +
                "        pq.offer(10);\n" +
                "        pq.offer(30);\n" +
                "        pq.offer(20);\n" +
                "        System.out.println(\"Min: \" + pq.peek());\n" +
                "        while (!pq.isEmpty()) {\n" +
                "            System.out.print(pq.poll() + \" \");\n" +
                "        }\n" +
                "        System.out.println();\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Generate Binary Numbers", "Generate first N binary numbers using a queue.", "QUEUE", "O(N)", "O(N)", "EASY",
                "import java.util.Queue;\n" +
                "import java.util.LinkedList;\n" +
                "public class GenerateBinaryNumbers {\n" +
                "    public static void main(String[] args) {\n" +
                "        int n = 5;\n" +
                "        Queue<String> queue = new LinkedList<>();\n" +
                "        queue.offer(\"1\");\n" +
                "        for (int i = 0; i < n; i++) {\n" +
                "            String front = queue.poll();\n" +
                "            System.out.print(front + \" \");\n" +
                "            queue.offer(front + \"0\");\n" +
                "            queue.offer(front + \"1\");\n" +
                "        }\n" +
                "        System.out.println();\n" +
                "    }\n" +
                "}");

        saveAlgorithm("Sliding Window Maximum (Deque)", "Find maximum in each window of size K using a monotonic deque.", "QUEUE", "O(N)", "O(K)", "HARD",
                "import java.util.ArrayDeque;\n" +
                "import java.util.Deque;\n" +
                "public class SlidingWindowMaxDeque {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {1, 3, -1, -3, 5, 3, 6, 7};\n" +
                "        int k = 3;\n" +
                "        int n = arr.length;\n" +
                "        Deque<Integer> deque = new ArrayDeque<>();\n" +
                "        for (int i = 0; i < n; i++) {\n" +
                "            while (!deque.isEmpty() && deque.peekFirst() < i - k + 1) deque.pollFirst();\n" +
                "            while (!deque.isEmpty() && arr[deque.peekLast()] < arr[i]) deque.pollLast();\n" +
                "            deque.offerLast(i);\n" +
                "            if (i >= k - 1) System.out.print(arr[deque.peekFirst()] + \" \");\n" +
                "        }\n" +
                "        System.out.println();\n" +
                "    }\n" +
                "}");

        saveAlgorithm("BFS Queue Processing", "Breadth-First Search using a Queue to traverse a graph level by level.", "QUEUE", "O(V+E)", "O(V)", "MEDIUM",
                "import java.util.*;\n" +
                "public class BfsQueue {\n" +
                "    public static void main(String[] args) {\n" +
                "        int vertices = 6;\n" +
                "        List<List<Integer>> adj = new ArrayList<>();\n" +
                "        for (int i = 0; i < vertices; i++) adj.add(new ArrayList<>());\n" +
                "        adj.get(0).add(1); adj.get(0).add(2);\n" +
                "        adj.get(1).add(3); adj.get(1).add(4);\n" +
                "        adj.get(2).add(5);\n" +
                "        boolean[] visited = new boolean[vertices];\n" +
                "        Queue<Integer> queue = new LinkedList<>();\n" +
                "        queue.offer(0);\n" +
                "        visited[0] = true;\n" +
                "        while (!queue.isEmpty()) {\n" +
                "            int node = queue.poll();\n" +
                "            System.out.print(node + \" \");\n" +
                "            for (int neighbor : adj.get(node)) {\n" +
                "                if (!visited[neighbor]) {\n" +
                "                    visited[neighbor] = true;\n" +
                "                    queue.offer(neighbor);\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println();\n" +
                "    }\n" +
                "}");

        log.info("Queue algorithms seeded.");
    }

    // ══════════════════════════════════════════════════════════════════
    // STACK PRACTICE PROBLEMS
    // ══════════════════════════════════════════════════════════════════
    private void seedStackPracticeProblems() {
        log.info("Seeding Stack practice problems...");

        saveProblem("Push and Pop",
                "Create a stack, push elements 10, 20, 30 into it, then pop and print all elements until empty (each on a new line).",
                "EASY", "STACK",
                "import java.util.Stack;\n" +
                "public class PracticeStackPush {\n" +
                "    public static void main(String[] args) {\n" +
                "        Stack<Integer> stack = new Stack<>();\n" +
                "        // TODO: Push 10, 20, 30. Then pop until empty and print each.\n\n" +
                "    }\n" +
                "}",
                "import java.util.Stack;\n" +
                "public class PracticeStackPush {\n" +
                "    public static void main(String[] args) {\n" +
                "        Stack<Integer> stack = new Stack<>();\n" +
                "        stack.push(10);\n" +
                "        stack.push(20);\n" +
                "        stack.push(30);\n" +
                "        while (!stack.isEmpty()) {\n" +
                "            System.out.println(stack.pop());\n" +
                "        }\n" +
                "    }\n" +
                "}",
                "30\n20\n10",
                "PracticeStackPush");

        saveProblem("Valid Parentheses",
                "Check if the string \"({[]}\" has balanced parentheses. Print true or false.",
                "EASY", "STACK",
                "import java.util.Stack;\n" +
                "public class PracticeBalancedParen {\n" +
                "    public static void main(String[] args) {\n" +
                "        String s = \"({[]})\";\n" +
                "        boolean valid = false;\n" +
                "        // TODO: Check balanced parentheses\n\n" +
                "        System.out.println(valid);\n" +
                "    }\n" +
                "}",
                "import java.util.Stack;\n" +
                "public class PracticeBalancedParen {\n" +
                "    public static void main(String[] args) {\n" +
                "        String s = \"({[]})\";\n" +
                "        Stack<Character> stack = new Stack<>();\n" +
                "        boolean valid = true;\n" +
                "        for (char c : s.toCharArray()) {\n" +
                "            if (c == '(' || c == '{' || c == '[') { stack.push(c); }\n" +
                "            else {\n" +
                "                if (stack.isEmpty()) { valid = false; break; }\n" +
                "                char t = stack.pop();\n" +
                "                if ((c == ')' && t != '(') || (c == '}' && t != '{') || (c == ']' && t != '[')) { valid = false; break; }\n" +
                "            }\n" +
                "        }\n" +
                "        System.out.println(valid && stack.isEmpty());\n" +
                "    }\n" +
                "}",
                "true",
                "PracticeBalancedParen");

        saveProblem("Next Greater Element",
                "Find NGE for arr = {4, 5, 2, 10, 8}. Print the result array in format [n1, n2, ...].",
                "MEDIUM", "STACK",
                "import java.util.Stack;\n" +
                "import java.util.Arrays;\n" +
                "public class PracticeNge {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {4, 5, 2, 10, 8};\n" +
                "        int[] nge = new int[arr.length];\n" +
                "        Arrays.fill(nge, -1);\n" +
                "        // TODO: Fill nge using a stack\n\n" +
                "        System.out.println(Arrays.toString(nge));\n" +
                "    }\n" +
                "}",
                "import java.util.Stack;\n" +
                "import java.util.Arrays;\n" +
                "public class PracticeNge {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] arr = {4, 5, 2, 10, 8};\n" +
                "        int n = arr.length;\n" +
                "        int[] nge = new int[n];\n" +
                "        Arrays.fill(nge, -1);\n" +
                "        Stack<Integer> stack = new Stack<>();\n" +
                "        for (int i = 0; i < n; i++) {\n" +
                "            while (!stack.isEmpty() && arr[stack.peek()] < arr[i]) nge[stack.pop()] = arr[i];\n" +
                "            stack.push(i);\n" +
                "        }\n" +
                "        System.out.println(Arrays.toString(nge));\n" +
                "    }\n" +
                "}",
                "[5, 10, 10, -1, -1]",
                "PracticeNge");

        saveProblem("Min Stack",
                "Design a MinStack. Push 5, 3, 7, 1. Print getMin(), pop, print getMin().",
                "MEDIUM", "STACK",
                "import java.util.Stack;\n" +
                "public class PracticeMinStack {\n" +
                "    // TODO: Implement MinStack\n" +
                "    public static void main(String[] args) {\n" +
                "        // Push 5, 3, 7, 1 and print getMin() before and after popping 1\n" +
                "    }\n" +
                "}",
                "import java.util.Stack;\n" +
                "public class PracticeMinStack {\n" +
                "    Stack<Integer> stack = new Stack<>();\n" +
                "    Stack<Integer> minSt = new Stack<>();\n" +
                "    void push(int x) {\n" +
                "        stack.push(x);\n" +
                "        if (minSt.isEmpty() || x <= minSt.peek()) minSt.push(x);\n" +
                "    }\n" +
                "    int pop() { int v = stack.pop(); if (v == minSt.peek()) minSt.pop(); return v; }\n" +
                "    int getMin() { return minSt.peek(); }\n" +
                "    public static void main(String[] args) {\n" +
                "        PracticeMinStack ms = new PracticeMinStack();\n" +
                "        ms.push(5); ms.push(3); ms.push(7); ms.push(1);\n" +
                "        System.out.println(ms.getMin());\n" +
                "        ms.pop();\n" +
                "        System.out.println(ms.getMin());\n" +
                "    }\n" +
                "}",
                "1\n3",
                "PracticeMinStack");

        log.info("Stack practice problems seeded.");
    }

    // ══════════════════════════════════════════════════════════════════
    // QUEUE PRACTICE PROBLEMS
    // ══════════════════════════════════════════════════════════════════
    private void seedQueuePracticeProblems() {
        log.info("Seeding Queue practice problems...");

        saveProblem("Enqueue and Dequeue",
                "Use a Queue, enqueue 10, 20, 30. Dequeue and print all elements until empty (each on new line).",
                "EASY", "QUEUE",
                "import java.util.*;\n" +
                "public class PracticeQueueBasic {\n" +
                "    public static void main(String[] args) {\n" +
                "        Queue<Integer> queue = new LinkedList<>();\n" +
                "        // TODO: Enqueue 10, 20, 30. Dequeue all and print.\n\n" +
                "    }\n" +
                "}",
                "import java.util.*;\n" +
                "public class PracticeQueueBasic {\n" +
                "    public static void main(String[] args) {\n" +
                "        Queue<Integer> queue = new LinkedList<>();\n" +
                "        queue.offer(10); queue.offer(20); queue.offer(30);\n" +
                "        while (!queue.isEmpty()) System.out.println(queue.poll());\n" +
                "    }\n" +
                "}",
                "10\n20\n30",
                "PracticeQueueBasic");

        saveProblem("Generate Binary Numbers",
                "Generate the first 5 binary numbers using a Queue. Print each on a new line.",
                "EASY", "QUEUE",
                "import java.util.*;\n" +
                "public class PracticeBinaryNums {\n" +
                "    public static void main(String[] args) {\n" +
                "        int n = 5;\n" +
                "        Queue<String> queue = new LinkedList<>();\n" +
                "        // TODO: Generate first n binary numbers\n\n" +
                "    }\n" +
                "}",
                "import java.util.*;\n" +
                "public class PracticeBinaryNums {\n" +
                "    public static void main(String[] args) {\n" +
                "        int n = 5;\n" +
                "        Queue<String> queue = new LinkedList<>();\n" +
                "        queue.offer(\"1\");\n" +
                "        for (int i = 0; i < n; i++) {\n" +
                "            String front = queue.poll();\n" +
                "            System.out.println(front);\n" +
                "            queue.offer(front + \"0\");\n" +
                "            queue.offer(front + \"1\");\n" +
                "        }\n" +
                "    }\n" +
                "}",
                "1\n10\n11\n100\n101",
                "PracticeBinaryNums");

        saveProblem("Circular Queue",
                "Implement a CircularQueue with capacity 4. Enqueue 10, 20, 30. Dequeue once. Enqueue 40. Print size.",
                "MEDIUM", "QUEUE",
                "public class PracticeCircularQueue {\n" +
                "    // TODO: Implement CircularQueue\n" +
                "    public static void main(String[] args) {\n" +
                "        // enqueue 10,20,30; dequeue; enqueue 40; print size\n" +
                "    }\n" +
                "}",
                "public class PracticeCircularQueue {\n" +
                "    int[] arr; int front = 0, rear = 0, size = 0;\n" +
                "    PracticeCircularQueue(int cap) { arr = new int[cap]; }\n" +
                "    void enqueue(int x) { if (size == arr.length) return; arr[rear] = x; rear = (rear+1)%arr.length; size++; }\n" +
                "    int dequeue() { if (size == 0) return -1; int v = arr[front]; front = (front+1)%arr.length; size--; return v; }\n" +
                "    public static void main(String[] args) {\n" +
                "        PracticeCircularQueue cq = new PracticeCircularQueue(4);\n" +
                "        cq.enqueue(10); cq.enqueue(20); cq.enqueue(30);\n" +
                "        cq.dequeue();\n" +
                "        cq.enqueue(40);\n" +
                "        System.out.println(cq.size);\n" +
                "    }\n" +
                "}",
                "3",
                "PracticeCircularQueue");

        saveProblem("Priority Queue Min Extraction",
                "Use PriorityQueue to extract minimum from {40, 10, 30, 20}. Print extracted values in order.",
                "MEDIUM", "QUEUE",
                "import java.util.PriorityQueue;\n" +
                "public class PracticePriorityQ {\n" +
                "    public static void main(String[] args) {\n" +
                "        PriorityQueue<Integer> pq = new PriorityQueue<>();\n" +
                "        // TODO: Add 40, 10, 30, 20. Poll and print until empty.\n\n" +
                "    }\n" +
                "}",
                "import java.util.PriorityQueue;\n" +
                "public class PracticePriorityQ {\n" +
                "    public static void main(String[] args) {\n" +
                "        PriorityQueue<Integer> pq = new PriorityQueue<>();\n" +
                "        pq.offer(40); pq.offer(10); pq.offer(30); pq.offer(20);\n" +
                "        while (!pq.isEmpty()) System.out.println(pq.poll());\n" +
                "    }\n" +
                "}",
                "10\n20\n30\n40",
                "PracticePriorityQ");

        log.info("Queue practice problems seeded.");
    }
}
