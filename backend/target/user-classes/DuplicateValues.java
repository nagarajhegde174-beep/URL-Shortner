public class DuplicateValues {
    public static void main(String[] args) {
        int[] arr = {5, 5, 5, 5};
        int count = 0;
        for (int x : arr) {
            if (x == 5) count++;
        }
        System.out.println("Count: " + count);
    }
}