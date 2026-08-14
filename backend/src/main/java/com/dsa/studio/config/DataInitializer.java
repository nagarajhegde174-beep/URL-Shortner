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
}
