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

import java.util.List;

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

        // 3. Seed Practice Problems
        if (practiceProblemRepository.count() == 0) {
            seedPracticeProblems();
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
