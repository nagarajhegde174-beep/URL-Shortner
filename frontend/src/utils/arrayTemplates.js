export const ARRAY_OPERATIONS = {
  traversal: {
    name: "Traversal",
    className: "ArrayTraversal",
    explanation: "Array Traversal involves visiting each element of the array exactly once, usually from index 0 to size-1, to perform an operation such as printing, summing, or modifying the values.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class ArrayTraversal {
    public static void main(String[] args) {
        int[] arr = {12, 34, 45, 9, 8, 90, 3};
        int iterations = 0;
        
        System.out.println("Starting array traversal:");
        for (int i = 0; i < arr.length; i++) {
            iterations++;
            System.out.println("Element at index " + i + ": " + arr[i]);
        }
        System.out.println("Total iterations: " + iterations);
    }
}`,
    interviewQuestions: [
      "Find the average/sum of all elements in an array.",
      "Print elements at odd indices."
    ],
    commonMistakes: [
      "Off-by-one errors: using i <= arr.length in the loop condition, causing ArrayIndexOutOfBoundsException.",
      "Assuming array index starts at 1 instead of 0."
    ],
    optimizedVersion: `// Traversal is inherently O(N) as all elements must be visited.
// However, memory layout is cache-friendly since elements are stored contiguously.
public class OptimizedTraversal {
    public static void traverse(int[] arr) {
        // Using enhanced for-loop (for-each) for cleaner syntax
        for (int val : arr) {
            // process val
        }
    }
}`
  },

  insert: {
    name: "Insert",
    className: "ArrayInsert",
    explanation: "Inserting an element at a specific index in a static array requires shifting all elements after that index to the right to make space for the new element. The array must have extra capacity to accommodate the new element.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class ArrayInsert {
    public static void main(String[] args) {
        int[] arr = {10, 20, 30, 40, 50, 0}; // Extra space at end
        int size = 5; // Current number of elements
        int element = 25;
        int insertIndex = 2;
        int iterations = 0;
        int swaps = 0; // Number of shift operations
        
        System.out.println("Inserting " + element + " at index " + insertIndex);
        for (int i = size - 1; i >= insertIndex; i--) {
            iterations++;
            arr[i + 1] = arr[i]; // Shift element right
            swaps++;
        }
        arr[insertIndex] = element; // Insert new element
        
        System.out.print("Array after insertion: ");
        for (int x : arr) {
            System.out.print(x + " ");
        }
        System.out.println();
    }
}`,
    interviewQuestions: [
      "Insert an element in a sorted array maintaining order.",
      "Explain the cost of inserting at the beginning vs the end of an array."
    ],
    commonMistakes: [
      "Shifting elements from left-to-right instead of right-to-left, which overwrites elements with their preceding values.",
      "Attempting to insert into a full array without resizing, leading to memory corruption or exception."
    ],
    optimizedVersion: `// To optimize insertion, dynamic arrays (like ArrayList) double their capacity when full,
// but shifting is still O(N). Insert at end is O(1) amortized.
public class ArrayInsertOptimized {
    public static int[] insertAt(int[] arr, int size, int element, int index) {
        if (size >= arr.length) {
            int[] newArr = new int[arr.length * 2];
            System.arraycopy(arr, 0, newArr, 0, index);
            newArr[index] = element;
            System.arraycopy(arr, index, newArr, index + 1, size - index);
            return newArr;
        }
        System.arraycopy(arr, index, arr, index + 1, size - index);
        arr[index] = element;
        return arr;
    }
}`
  },

  delete: {
    name: "Delete",
    className: "ArrayDelete",
    explanation: "Deleting an element at a given index requires shifting all subsequent elements one position to the left to fill the gap, then clearing the last slot.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class ArrayDelete {
    public static void main(String[] args) {
        int[] arr = {10, 20, 30, 40, 50};
        int deleteIndex = 2; // Deleting element 30
        int iterations = 0;
        int swaps = 0; // Number of left shifts
        
        System.out.println("Deleting element at index " + deleteIndex);
        for (int i = deleteIndex; i < arr.length - 1; i++) {
            iterations++;
            arr[i] = arr[i + 1]; // Shift element left
            swaps++;
        }
        arr[arr.length - 1] = 0; // Clear last element
        
        System.out.print("Array after deletion: ");
        for (int x : arr) {
            System.out.print(x + " ");
        }
        System.out.println();
    }
}`,
    interviewQuestions: [
      "Remove all occurrences of a specific element in-place.",
      "Delete element at index O(1) if array order doesn't matter."
    ],
    commonMistakes: [
      "Forgetting to clear the last element, leaving a duplicate of the second-to-last element.",
      "ArrayIndexOutOfBoundsException when accessing i + 1 on the last index."
    ],
    optimizedVersion: `// If order doesn't matter, we can replace deleted element with the last element.
// This reduces deletion complexity to O(1).
public class ArrayDeleteOptimized {
    public static void deleteUnordered(int[] arr, int size, int index) {
        arr[index] = arr[size - 1];
        arr[size - 1] = 0; // Clear last
    }
}`
  },

  update: {
    name: "Update",
    className: "ArrayUpdate",
    explanation: "Updating an array element changes the value at a specific index. Since arrays support random access, updates are completed instantly in O(1) constant time.",
    timeComplexity: "O(1)",
    spaceComplexity: "O(1)",
    javaCode: `public class ArrayUpdate {
    public static void main(String[] args) {
        int[] arr = {10, 20, 30, 40, 50};
        int updateIndex = 3;
        int newValue = 99;
        
        System.out.println("Updating index " + updateIndex + " to " + newValue);
        arr[updateIndex] = newValue;
        
        System.out.print("Array after update: ");
        for (int x : arr) {
            System.out.print(x + " ");
        }
        System.out.println();
    }
}`,
    interviewQuestions: [
      "Update elements in an array satisfying a specific condition."
    ],
    commonMistakes: [
      "Accessing indices that do not exist (e.g. index < 0 or index >= arr.length)."
    ],
    optimizedVersion: `// Updates are already O(1), the fastest possible.
public class ArrayUpdateOptimized {
    public static void update(int[] arr, int index, int value) {
        if (index >= 0 && index < arr.length) {
            arr[index] = value;
        }
    }
}`
  },

  search: {
    name: "Search",
    className: "ArraySearch",
    explanation: "Linear Search checks every element of the array from index 0 sequentially until it finds the target value, returning its index.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class ArraySearch {
    public static void main(String[] args) {
        int[] arr = {12, 34, 45, 9, 8, 90, 3};
        int target = 9;
        int index = -1;
        int iterations = 0;
        int comparisons = 0;
        
        for (int i = 0; i < arr.length; i++) {
            iterations++;
            comparisons++;
            if (arr[i] == target) {
                index = i;
                break;
            }
        }
        System.out.println("Target found at index: " + index);
    }
}`,
    interviewQuestions: [
      "Search in an unsorted array.",
      "How to search efficiently in a sorted array? (Binary Search)"
    ],
    commonMistakes: [
      "Returning -1 inside the loop instead of after completing the full loop iteration.",
      "Using == for object comparison instead of .equals()."
    ],
    optimizedVersion: `// If the array is sorted, Binary Search is O(log N).
// For unsorted arrays, we can search from both ends to slightly reduce iteration cost in practice.
public class SearchOptimized {
    public static int linearSearchBiDirectional(int[] arr, int target) {
        int left = 0, right = arr.length - 1;
        while (left <= right) {
            if (arr[left] == target) return left;
            if (arr[right] == target) return right;
            left++;
            right--;
        }
        return -1;
    }
}`
  },

  reverse: {
    name: "Reverse",
    className: "ArrayReverse",
    explanation: "Reversing an array swaps elements from the beginning with elements at the end, moving inwards using two pointers until they meet in the center.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class ArrayReverse {
    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5, 6};
        int start = 0;
        int end = arr.length - 1;
        int iterations = 0;
        int swaps = 0;
        
        while (start < end) {
            iterations++;
            // Swap start and end elements
            int temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
            swaps++;
            
            start++;
            end--;
        }
        System.out.print("Reversed Array: ");
        for (int x : arr) {
            System.out.print(x + " ");
        }
        System.out.println();
    }
}`,
    interviewQuestions: [
      "Reverse an array in-place without using extra memory.",
      "Reverse a subsegment of an array between indices L and R."
    ],
    commonMistakes: [
      "Looping through the entire array size (0 to n) which swaps elements twice, restoring the original array order.",
      "Using start <= end which performs an unnecessary swap of the middle element with itself."
    ],
    optimizedVersion: `// In-place two-pointer swapping is already optimal.
public class ReverseOptimized {
    public static void reverse(int[] arr, int start, int end) {
        while (start < end) {
            int temp = arr[start];
            arr[start++] = arr[end];
            arr[end--] = temp;
        }
    }
}`
  },

  rotateLeft: {
    name: "Rotate Left",
    className: "ArrayRotateLeft",
    explanation: "Left rotation shifts all elements one position to the left. The first element is stored temporarily and then placed at the very end.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class ArrayRotateLeft {
    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5};
        int iterations = 0;
        int swaps = 0;
        
        int first = arr[0];
        for (int i = 0; i < arr.length - 1; i++) {
            iterations++;
            arr[i] = arr[i + 1]; // Shift left
            swaps++;
        }
        arr[arr.length - 1] = first; // Wrap first to last
        
        System.out.print("Rotated Left: ");
        for (int x : arr) {
            System.out.print(x + " ");
        }
        System.out.println();
    }
}`,
    interviewQuestions: [
      "Rotate an array left by D steps in O(N) time and O(1) space.",
      "Explain left rotation applications (e.g. queue scheduling)."
    ],
    commonMistakes: [
      "Not caching the first element before beginning shifts, resulting in the first element getting lost.",
      "Off-by-one error when copying arr[i + 1] at the last index."
    ],
    optimizedVersion: `// Rotate left by D positions using block reversal method.
// Reversing portions of the array yields O(N) time and O(1) space.
public class RotateLeftOptimized {
    public static void rotate(int[] arr, int d) {
        int n = arr.length;
        d = d % n; // handle d > n
        reverse(arr, 0, d - 1);
        reverse(arr, d, n - 1);
        reverse(arr, 0, n - 1);
    }
    private static void reverse(int[] arr, int start, int end) {
        while (start < end) {
            int temp = arr[start];
            arr[start++] = arr[end];
            arr[end--] = temp;
        }
    }
}`
  },

  rotateRight: {
    name: "Rotate Right",
    className: "ArrayRotateRight",
    explanation: "Right rotation shifts all elements one position to the right. The last element is saved first and then placed in the first slot.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class ArrayRotateRight {
    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5};
        int iterations = 0;
        int swaps = 0;
        
        int last = arr[arr.length - 1];
        for (int i = arr.length - 1; i > 0; i--) {
            iterations++;
            arr[i] = arr[i - 1]; // Shift right
            swaps++;
        }
        arr[0] = last; // Wrap last to first
        
        System.out.print("Rotated Right: ");
        for (int x : arr) {
            System.out.print(x + " ");
        }
        System.out.println();
    }
}`,
    interviewQuestions: [
      "Rotate an array right by K steps efficiently.",
      "Reconstruct array after right rotations."
    ],
    commonMistakes: [
      "Shifting elements from left-to-right starting at index 0, which duplicates arr[0] across the entire array.",
      "Not wrapping indices properly when shifting by arbitrary values of D."
    ],
    optimizedVersion: `// Rotate right by D positions using triple reversal.
public class RotateRightOptimized {
    public static void rotate(int[] arr, int d) {
        int n = arr.length;
        d = d % n;
        reverse(arr, 0, n - 1);
        reverse(arr, 0, d - 1);
        reverse(arr, d, n - 1);
    }
    private static void reverse(int[] arr, int start, int end) {
        while (start < end) {
            int temp = arr[start];
            arr[start++] = arr[end];
            arr[end--] = temp;
        }
    }
}`
  },

  max: {
    name: "Maximum",
    className: "ArrayMax",
    explanation: "Finding the maximum element requires initializing a placeholder variable 'max' to the first element, then scanning the rest of the array to check if a larger value is found.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class ArrayMax {
    public static void main(String[] args) {
        int[] arr = {12, 34, 45, 9, 8, 90, 3};
        int max = arr[0];
        int iterations = 0;
        int comparisons = 0;
        
        for (int i = 1; i < arr.length; i++) {
            iterations++;
            comparisons++;
            if (arr[i] > max) {
                max = arr[i];
            }
        }
        System.out.println("Maximum value: " + max);
    }
}`,
    interviewQuestions: [
      "Find the second largest element in an array.",
      "Find maximum and minimum in minimum comparisons."
    ],
    commonMistakes: [
      "Initializing max = 0. If all elements in the array are negative, the program will incorrectly output 0.",
      "Comparing values using index variables instead of the element value (e.g. if (i > max))."
    ],
    optimizedVersion: `// Scan array in pairs to find Min and Max simultaneously in 3*(N/2) comparisons.
public class MinMaxOptimized {
    public static int[] getMinMax(int[] arr) {
        int n = arr.length;
        int min, max;
        int i = 0;
        if (n % 2 == 0) {
            if (arr[0] > arr[1]) {
                max = arr[0]; min = arr[1];
            } else {
                max = arr[1]; min = arr[0];
            }
            i = 2;
        } else {
            max = arr[0]; min = arr[0];
            i = 1;
        }
        while (i < n - 1) {
            if (arr[i] > arr[i + 1]) {
                if (arr[i] > max) max = arr[i];
                if (arr[i + 1] < min) min = arr[i + 1];
            } else {
                if (arr[i + 1] > max) max = arr[i + 1];
                if (arr[i] < min) min = arr[i];
            }
            i += 2;
        }
        return new int[]{min, max};
    }
}`
  },

  min: {
    name: "Minimum",
    className: "ArrayMin",
    explanation: "Finding the minimum element scans all values, comparing each element to the current smallest value recorded and updating it whenever a smaller element is discovered.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class ArrayMin {
    public static void main(String[] args) {
        int[] arr = {12, 34, 45, 9, 8, 90, 3};
        int min = arr[0];
        int iterations = 0;
        int comparisons = 0;
        
        for (int i = 1; i < arr.length; i++) {
            iterations++;
            comparisons++;
            if (arr[i] < min) {
                min = arr[i];
            }
        }
        System.out.println("Minimum value: " + min);
    }
}`,
    interviewQuestions: [
      "Find the index of the minimum element.",
      "Check if an array contains duplicate elements."
    ],
    commonMistakes: [
      "Initializing min to a small default value like 0, which fails if all array numbers are positive.",
      "Starting loop at index 0 when 0 is already compared, wasting 1 comparison."
    ],
    optimizedVersion: `// Standard iteration is optimal, but can be split using recursion (Divide and Conquer).
public class MinOptimized {
    public static int findMin(int[] arr, int low, int high) {
        if (low == high) return arr[low];
        int mid = (low + high) / 2;
        return Math.min(findMin(arr, low, mid), findMin(arr, mid + 1, high));
    }
}`
  }
};

export const ARRAY_PATTERNS = {
  twopointer: {
    name: "Two Pointer",
    className: "ArrayTwoPointer",
    explanation: "The Two Pointer pattern maintains two cursor indexes (e.g. left and right) that move toward each other to process elements in a sorted array, reducing nested O(N^2) loops to linear O(N) time.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class ArrayTwoPointer {
    public static void main(String[] args) {
        int[] arr = {2, 7, 11, 15};
        int target = 9;
        int left = 0;
        int right = arr.length - 1;
        int iterations = 0;
        int comparisons = 0;
        
        System.out.println("Searching for target sum " + target + ":");
        while (left < right) {
            iterations++;
            comparisons++;
            int sum = arr[left] + arr[right];
            if (sum == target) {
                System.out.println("Pair found: " + arr[left] + " + " + arr[right]);
                break;
            } else if (sum < target) {
                left++;
            } else {
                right--;
            }
        }
    }
}`,
    interviewQuestions: [
      "Two Sum in a sorted array.",
      "Container with most water."
    ],
    commonMistakes: [
      "Using two pointers on an unsorted array, where target sum decisions based on larger/smaller values will fail.",
      "Infinite loops by failing to increment 'left' or decrement 'right' pointers."
    ],
    optimizedVersion: `// Two-pointers is already O(N) and highly optimized.
public class TwoPointerSolution {
    public static boolean hasTargetSum(int[] arr, int target) {
        int l = 0, r = arr.length - 1;
        while (l < r) {
            int s = arr[l] + arr[r];
            if (s == target) return true;
            else if (s < target) l++;
            else r--;
        }
        return false;
    }
}`
  },

  slidingwindow: {
    name: "Sliding Window",
    className: "ArraySlidingWindow",
    explanation: "The Sliding Window pattern is used to track subarrays or subsegments. Rather than recomputing metrics for each window, it slides the window by adding the incoming element and subtracting the outgoing element.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class ArraySlidingWindow {
    public static void main(String[] args) {
        int[] arr = {2, 1, 5, 1, 3, 2};
        int k = 3;
        int maxSum = 0;
        int windowSum = 0;
        int iterations = 0;
        
        // 1. Initial window sum
        for (int i = 0; i < k; i++) {
            iterations++;
            windowSum += arr[i];
        }
        maxSum = windowSum;
        
        // 2. Slide window across array
        for (int i = k; i < arr.length; i++) {
            iterations++;
            windowSum += arr[i] - arr[i - k]; // Slide right
            if (windowSum > maxSum) {
                maxSum = windowSum;
            }
        }
        System.out.println("Maximum window sum: " + maxSum);
    }
}`,
    interviewQuestions: [
      "Maximum sum subarray of size K.",
      "Longest substring without repeating characters."
    ],
    commonMistakes: [
      "Forgetting to subtract the element falling out of the left side of the window (arr[i - k]).",
      "Off-by-one errors in sliding window boundaries (e.g. index >= arr.length)."
    ],
    optimizedVersion: `// Sliding window avoids nested iterations, keeping it O(N).
public class SlidingWindowOptimized {
    public static int findMaxSumSubarray(int[] arr, int k) {
        int n = arr.length;
        if (n < k) return -1;
        int res = 0;
        for (int i = 0; i < k; i++) res += arr[i];
        int curr = res;
        for (int i = k; i < n; i++) {
            curr += arr[i] - arr[i - k];
            res = Math.max(res, curr);
        }
        return res;
    }
}`
  },

  prefixsum: {
    name: "Prefix Sum",
    className: "ArrayPrefixSum",
    explanation: "Prefix Sum builds a running sum array, where prefix[i] = arr[0] + arr[1] + ... + arr[i]. This allows O(1) time range sum query evaluations between index L and R.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(N)",
    javaCode: `public class ArrayPrefixSum {
    public static void main(String[] args) {
        int[] arr = {3, 1, 2, 5, 4};
        int[] prefix = new int[arr.length];
        int iterations = 0;
        
        prefix[0] = arr[0];
        for (int i = 1; i < arr.length; i++) {
            iterations++;
            prefix[i] = prefix[i - 1] + arr[i];
        }
        
        System.out.print("Prefix Sum Array: ");
        for (int val : prefix) {
            System.out.print(val + " ");
        }
        System.out.println();
    }
}`,
    interviewQuestions: [
      "Range Sum Query (Immutable).",
      "Find pivot index where left sum equals right sum."
    ],
    commonMistakes: [
      "Off-by-one errors when querying sum of range [L, R] -> range sum = prefix[R] - prefix[L - 1]. Handling L=0 case is essential.",
      "Integer overflow during sum accumulation in prefix arrays."
    ],
    optimizedVersion: `// Space can be optimized to O(1) if we overwrite the input array to store prefix sums.
public class PrefixSumOptimized {
    public static int getRangeSum(int[] prefix, int L, int R) {
        if (L == 0) return prefix[R];
        return prefix[R] - prefix[L - 1];
    }
}`
  },

  suffixsum: {
    name: "Suffix Sum",
    className: "ArraySuffixSum",
    explanation: "Suffix Sum computes running cumulative sums starting from the rightmost element (index size-1) down to index 0.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(N)",
    javaCode: `public class ArraySuffixSum {
    public static void main(String[] args) {
        int[] arr = {3, 1, 2, 5, 4};
        int[] suffix = new int[arr.length];
        int iterations = 0;
        int n = arr.length;
        
        suffix[n - 1] = arr[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            iterations++;
            suffix[i] = suffix[i + 1] + arr[i];
        }
        
        System.out.print("Suffix Sum Array: ");
        for (int val : suffix) {
            System.out.print(val + " ");
        }
        System.out.println();
    }
}`,
    interviewQuestions: [
      "Product of array except self.",
      "Evaluate right-side balance queries."
    ],
    commonMistakes: [
      "Starting the backward loop at index n-1 instead of n-2, which attempts to read suffix[n] and triggers exception."
    ],
    optimizedVersion: `// Space can be optimized by storing suffix sums in-place.
public class SuffixSumInPlace {
    public static void compute(int[] arr) {
        for (int i = arr.length - 2; i >= 0; i--) {
            arr[i] += arr[i + 1];
        }
    }
}`
  },

  kadane: {
    name: "Kadane's Algorithm",
    className: "ArrayKadane",
    explanation: "Kadane's algorithm computes the maximum contiguous subarray sum in linear time. It maintains 'maxEndingHere' and updates 'maxSoFar' at each position.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class ArrayKadane {
    public static void main(String[] args) {
        int[] arr = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        int maxSoFar = arr[0];
        int maxEndingHere = arr[0];
        int iterations = 0;
        int comparisons = 0;
        
        for (int i = 1; i < arr.length; i++) {
            iterations++;
            maxEndingHere = maxEndingHere + arr[i];
            comparisons++;
            if (maxEndingHere < arr[i]) {
                maxEndingHere = arr[i];
            }
            comparisons++;
            if (maxSoFar < maxEndingHere) {
                maxSoFar = maxEndingHere;
            }
        }
        System.out.println("Max contiguous subarray sum: " + maxSoFar);
    }
}`,
    interviewQuestions: [
      "Maximum subarray sum.",
      "Find indices of the maximum sum contiguous subarray."
    ],
    commonMistakes: [
      "Initializing maxSoFar = 0. This incorrectly handles arrays consisting solely of negative numbers.",
      "Resetting maxEndingHere to 0 immediately before comparing it with the element value."
    ],
    optimizedVersion: `// Kadane's algorithm is already highly optimized.
public class KadaneSolution {
    public static int maxSubarraySum(int[] arr) {
        int max = arr[0], curr = arr[0];
        for (int i = 1; i < arr.length; i++) {
            curr = Math.max(arr[i], curr + arr[i]);
            max = Math.max(max, curr);
        }
        return max;
    }
}`
  },

  binarysearch: {
    name: "Binary Search",
    className: "ArrayBinarySearch",
    explanation: "Binary Search finds the index of a target element in a sorted array by checking the middle element. It halves the active search space at each iteration, achieving logarithmic runtime.",
    timeComplexity: "O(log N)",
    spaceComplexity: "O(1)",
    javaCode: `public class ArrayBinarySearch {
    public static void main(String[] args) {
        int[] arr = {2, 5, 8, 12, 16, 23, 38, 56, 72, 91};
        int target = 23;
        int low = 0;
        int high = arr.length - 1;
        int mid = -1;
        int iterations = 0;
        int comparisons = 0;
        
        while (low <= high) {
            iterations++;
            mid = (low + high) / 2;
            comparisons++;
            if (arr[mid] == target) {
                System.out.println("Target found at index: " + mid);
                break;
            }
            comparisons++;
            if (arr[mid] < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
    }
}`,
    interviewQuestions: [
      "Find first and last position of element in sorted array.",
      "Search in a rotated sorted array."
    ],
    commonMistakes: [
      "Using low < high instead of low <= high, which fails to inspect single element arrays or the final boundary index.",
      "Integer overflow: using mid = (low + high) / 2 instead of mid = low + (high - low) / 2 for extremely large array indices."
    ],
    optimizedVersion: `// Overflow-safe index calculation.
public class BinarySearchSafe {
    public static int search(int[] arr, int target) {
        int l = 0, h = arr.length - 1;
        while (l <= h) {
            int mid = l + (h - l) / 2; // Safe from integer overflow
            if (arr[mid] == target) return mid;
            else if (arr[mid] < target) l = mid + 1;
            else h = mid - 1;
        }
        return -1;
    }
}`
  }
};

export const COMPARISON_TEMPLATES = {
  linearSearch: {
    className: "ArraySearch",
    javaCode: `public class ArraySearch {
    public static void main(String[] args) {
        int[] arr = {2, 5, 8, 12, 16, 23, 38, 56, 72, 91};
        int target = 23;
        int iterations = 0;
        int comparisons = 0;
        int index = -1;
        
        for (int i = 0; i < arr.length; i++) {
            iterations++;
            comparisons++;
            if (arr[i] == target) {
                index = i;
                break;
            }
        }
        System.out.println("Found at: " + index);
    }
}`
  },
  binarySearch: {
    className: "ArrayBinarySearch",
    javaCode: `public class ArrayBinarySearch {
    public static void main(String[] args) {
        int[] arr = {2, 5, 8, 12, 16, 23, 38, 56, 72, 91};
        int target = 23;
        int low = 0;
        int high = arr.length - 1;
        int mid = -1;
        int iterations = 0;
        int comparisons = 0;
        
        while (low <= high) {
            iterations++;
            mid = (low + high) / 2;
            comparisons++;
            if (arr[mid] == target) {
                System.out.println("Found at: " + mid);
                break;
            }
            comparisons++;
            if (arr[mid] < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
    }
}`
  },
  bubbleSort: {
    className: "BubbleSort",
    javaCode: `public class BubbleSort {
    public static void main(String[] args) {
        int[] arr = {5, 1, 4, 2, 8};
        int iterations = 0;
        int comparisons = 0;
        int swaps = 0;
        int n = arr.length;
        
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                iterations++;
                comparisons++;
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swaps++;
                }
            }
        }
        System.out.println("Array sorted.");
    }
}`
  },
  selectionSort: {
    className: "SelectionSort",
    javaCode: `public class SelectionSort {
    public static void main(String[] args) {
        int[] arr = {5, 1, 4, 2, 8};
        int iterations = 0;
        int comparisons = 0;
        int swaps = 0;
        int n = arr.length;
        
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                iterations++;
                comparisons++;
                if (arr[j] < arr[minIdx]) {
                    minIdx = j;
                }
            }
            int temp = arr[minIdx];
            arr[minIdx] = arr[i];
            arr[i] = temp;
            swaps++;
        }
        System.out.println("Array sorted.");
    }
}`
  },
  mergeSort: {
    className: "MergeSort",
    javaCode: `public class MergeSort {
    static int iterations = 0;
    static int comparisons = 0;
    static int swaps = 0;
    
    public static void main(String[] args) {
        int[] arr = {5, 1, 4, 2, 8};
        sort(arr, 0, arr.length - 1);
        System.out.println("Sorted: " + arr.length);
    }
    
    public static void sort(int[] arr, int l, int r) {
        if (l < r) {
            int m = (l + r) / 2;
            sort(arr, l, m);
            sort(arr, m + 1, r);
            merge(arr, l, m, r);
        }
    }
    
    public static void merge(int[] arr, int l, int m, int r) {
        int n1 = m - l + 1;
        int n2 = r - m;
        int[] L = new int[n1];
        int[] R = new int[n2];
        for (int i = 0; i < n1; ++i) L[i] = arr[l + i];
        for (int j = 0; j < n2; ++j) R[j] = arr[m + 1 + j];
        
        int i = 0, j = 0;
        int k = l;
        while (i < n1 && j < n2) {
            iterations++;
            comparisons++;
            if (L[i] <= R[j]) {
                arr[k] = L[i];
                i++;
            } else {
                arr[k] = R[j];
                j++;
            }
            swaps++; // Copy count
            k++;
        }
        while (i < n1) {
            arr[k] = L[i];
            i++; k++; swaps++;
        }
        while (j < n2) {
            arr[k] = R[j];
            j++; k++; swaps++;
        }
    }
}`
  },
  quickSort: {
    className: "QuickSort",
    javaCode: `public class QuickSort {
    static int iterations = 0;
    static int comparisons = 0;
    static int swaps = 0;
    
    public static void main(String[] args) {
        int[] arr = {5, 1, 4, 2, 8};
        quickSort(arr, 0, arr.length - 1);
        System.out.println("Sorted: " + arr.length);
    }
    
    public static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }
    
    public static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            iterations++;
            comparisons++;
            if (arr[j] < pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                swaps++;
            }
        }
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        swaps++;
        return i + 1;
    }
}`
  }
};
