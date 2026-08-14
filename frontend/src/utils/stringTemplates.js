export const STRING_OPERATIONS = {
  traversal: {
    name: "Traversal",
    className: "StringTraversal",
    explanation: "String Traversal involves visiting each character of a string exactly once, usually from index 0 to length-1, using charAt() or by converting it to a char array.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class StringTraversal {
    public static void main(String[] args) {
        String str = "Hello World";
        int iterations = 0;
        
        for (int i = 0; i < str.length(); i++) {
            iterations++;
            char c = str.charAt(i);
            System.out.println("Character at " + i + ": " + c);
        }
    }
}`,
    interviewQuestions: [
      "Find the number of vowels and consonants in a string.",
      "Check if a string contains only digits."
    ],
    commonMistakes: [
      "Using index out of bounds: accessing str.charAt(str.length()) instead of str.length() - 1.",
      "Looping using string.length instead of calling the length() method."
    ],
    optimizedVersion: `public class OptimizedTraversal {
    public static void traverse(String str) {
        // Converting to char array is often faster for large strings as it avoids method call overhead of charAt()
        char[] chars = str.toCharArray();
        for (char c : chars) {
            // process c
        }
    }
}`
  },

  reverse: {
    name: "Reverse String",
    className: "StringReverse",
    explanation: "Reversing a string involves swapping characters from the start and end of the string moving towards the center using a two-pointer approach.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(N)",
    javaCode: `public class StringReverse {
    public static void main(String[] args) {
        String str = "Antigravity";
        char[] chars = str.toCharArray();
        int left = 0;
        int right = chars.length - 1;
        
        while (left < right) {
            char temp = chars[left];
            chars[left] = chars[right];
            chars[right] = temp;
            left++;
            right--;
        }
        System.out.println(new String(chars));
    }
}`,
    interviewQuestions: [
      "Reverse words in a given sentence.",
      "Reverse a string without using extra variables."
    ],
    commonMistakes: [
      "Attempting to modify the original string directly (strings are immutable in Java).",
      "Off-by-one errors in pointer initialization."
    ],
    optimizedVersion: `public class OptimizedReverse {
    public static String reverse(String str) {
        // StringBuilder utilizes an internal mutable char array, making it very memory efficient
        return new StringBuilder(str).reverse().toString();
    }
}`
  },

  palindrome: {
    name: "Palindrome",
    className: "StringPalindrome",
    explanation: "A string is a palindrome if it reads the same backward as forward. We can check this by comparing characters from start and end moving inwards.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class StringPalindrome {
    public static void main(String[] args) {
        String str = "racecar";
        int left = 0;
        int right = str.length() - 1;
        boolean isPal = true;
        
        while (left < right) {
            if (str.charAt(left) != str.charAt(right)) {
                isPal = false;
                break;
            }
            left++;
            right--;
        }
        System.out.println("Is Palindrome: " + isPal);
    }
}`,
    interviewQuestions: [
      "Check palindrome ignoring spaces and non-alphanumeric characters.",
      "Find the longest palindromic substring in a string."
    ],
    commonMistakes: [
      "Converting to string builder and reversing, which uses O(N) auxiliary space instead of O(1).",
      "Not handles empty or single-character strings as trivial palindromes."
    ],
    optimizedVersion: `public class OptimizedPalindrome {
    public static boolean isPalindrome(String str) {
        int left = 0, right = str.length() - 1;
        while (left < right) {
            // Ignore non-alphanumeric character helper check can be put here
            if (str.charAt(left) != str.charAt(right)) return false;
            left++;
            right--;
        }
        return true;
    }
}`
  },

  charFrequency: {
    name: "Character Frequency",
    className: "StringCharFreq",
    explanation: "Counting frequency of each character is commonly done using an integer array as a hash map, where index represents the ASCII/Unicode value.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class StringCharFreq {
    public static void main(String[] args) {
        String str = "success";
        int[] freq = new int[256];
        
        for (int i = 0; i < str.length(); i++) {
            freq[str.charAt(i)]++;
        }
        System.out.println("Frequency of s: " + freq['s']);
    }
}`,
    interviewQuestions: [
      "Find the first non-repeating character in a string.",
      "Sort characters of a string by their frequency."
    ],
    commonMistakes: [
      "Using HashMap for ASCII range when a simple 256-sized array is much faster and uses less memory.",
      "Assuming only lowercase letters without verifying inputs."
    ],
    optimizedVersion: `public class OptimizedCharFreq {
    public static int[] getFrequency(String str) {
        int[] freq = new int[256];
        char[] chars = str.toCharArray();
        for (char c : chars) {
            freq[c]++;
        }
        return freq;
    }
}`
  },

  anagram: {
    name: "Anagram",
    className: "StringAnagram",
    explanation: "Two strings are anagrams if they contain the same characters with the same frequencies. This is checked by maintaining a frequency count for one and decrementing for the other.",
    timeComplexity: "O(N)",
    spaceComplexity: "O(1)",
    javaCode: `public class StringAnagram {
    public static void main(String[] args) {
        String s1 = "listen";
        String s2 = "silent";
        boolean isAnagram = true;
        
        if (s1.length() != s2.length()) {
            isAnagram = false;
        } else {
            int[] counts = new int[256];
            for (int i = 0; i < s1.length(); i++) {
                counts[s1.charAt(i)]++;
                counts[s2.charAt(i)]--;
            }
            for (int c : counts) {
                if (c != 0) { 
                    isAnagram = false; 
                    break; 
                }
            }
        }
        System.out.println("Is Anagram: " + isAnagram);
    }
}`,
    interviewQuestions: [
      "Group anagrams together from a list of strings.",
      "Find all anagram substrings in a larger string."
    ],
    commonMistakes: [
      "Sorting both strings and comparing, which is O(N log N) time complexity compared to the linear O(N) frequency array approach.",
      "Failing to verify lengths before running counts loops."
    ],
    optimizedVersion: `public class OptimizedAnagram {
    public static boolean isAnagram(String s1, String s2) {
        if (s1.length() != s2.length()) return false;
        int[] counts = new int[26]; // for lowercase alphabet
        char[] c1 = s1.toCharArray();
        char[] c2 = s2.toCharArray();
        for (int i = 0; i < c1.length; i++) {
            counts[c1[i] - 'a']++;
            counts[c2[i] - 'a']--;
        }
        for (int val : counts) {
            if (val != 0) return false;
        }
        return true;
    }
}`
  },

  naiveMatch: {
    name: "Naive Match",
    className: "StringNaiveMatch",
    explanation: "The Naive pattern matching matches the pattern against all possible index offsets in the text. Slow but straightforward.",
    timeComplexity: "O(N * M)",
    spaceComplexity: "O(1)",
    javaCode: `public class StringNaiveMatch {
    public static void main(String[] args) {
        String text = "AABAACAADAABAAABDF";
        String pattern = "AABA";
        int n = text.length();
        int m = pattern.length();
        
        for (int i = 0; i <= n - m; i++) {
            int j;
            for (j = 0; j < m; j++) {
                if (text.charAt(i + j) != pattern.charAt(j)) break;
            }
            if (j == m) {
                System.out.println("Pattern found at: " + i);
            }
        }
    }
}`,
    interviewQuestions: [
      "Explain worst-case scenario for naive pattern matching.",
      "Implement substring search standard library method indexof."
    ],
    commonMistakes: [
      "Off-by-one errors in text-offset loop boundary: iterating up to i < n - m instead of i <= n - m."
    ],
    optimizedVersion: `// Optimized version uses Rabin-Karp or KMP algorithms.`
  },

  kmp: {
    name: "KMP Search",
    className: "StringKmp",
    explanation: "Knuth-Morris-Pratt (KMP) search precomputes a Longest Prefix Suffix (LPS) array of the pattern to skip redundant comparisons, executing pattern search in linear time.",
    timeComplexity: "O(N + M)",
    spaceComplexity: "O(M)",
    javaCode: `public class StringKmp {
    public static void main(String[] args) {
        String text = "ABABDABACDABABCABAB";
        String pattern = "ABABCABAB";
        int[] lps = {0, 0, 1, 2, 0, 1, 2, 3, 4}; // precomputed
        int i = 0, j = 0;
        
        while (i < text.length()) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++; j++;
            }
            if (j == pattern.length()) {
                System.out.println("Found pattern at: " + (i - j));
                j = lps[j - 1];
            } else if (i < text.length() && pattern.charAt(j) != text.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
    }
}`,
    interviewQuestions: [
      "Explain KMP LPS array generation algorithm.",
      "Compare Rabin-Karp vs KMP search."
    ],
    commonMistakes: [
      "Accessing out of bounds in lps array.",
      "Incorrectly incrementing index i when resetting pattern pointer j."
    ],
    optimizedVersion: `// Complete KMP algorithm including pre-computation step.`
  }
};

export const COMPARISON_TEMPLATES = {
  naiveMatch: {
    className: "StringNaiveMatch",
    javaCode: `public class StringNaiveMatch {
    public static void main(String[] args) {
        String text = "AABAACAADAABAAABDF";
        String pattern = "AABA";
        int n = text.length();
        int m = pattern.length();
        for (int i = 0; i <= n - m; i++) {
            int j;
            for (j = 0; j < m; j++) {
                if (text.charAt(i + j) != pattern.charAt(j)) break;
            }
            if (j == m) System.out.println("Pattern found at: " + i);
        }
    }
}`
  },
  kmp: {
    className: "StringKmp",
    javaCode: `public class StringKmp {
    public static void main(String[] args) {
        String text = "ABABDABACDABABCABAB";
        String pattern = "ABABCABAB";
        int[] lps = {0, 0, 1, 2, 0, 1, 2, 3, 4};
        int i = 0, j = 0;
        while (i < text.length()) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++; j++;
            }
            if (j == pattern.length()) {
                System.out.println("Found pattern at: " + (i - j));
                j = lps[j - 1];
            } else if (i < text.length() && pattern.charAt(j) != text.charAt(i)) {
                if (j != 0) j = lps[j - 1];
                else i++;
            }
        }
    }
}`
  }
};
