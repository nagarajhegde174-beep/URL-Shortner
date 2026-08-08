public class EmptyArray {
    public static void main(String[] args) {
        int[] arr = {};
        int iterations = 0;
        for (int i = 0; i < arr.length; i++) {
            iterations++;
        }
        System.out.println("Done: " + iterations);
    }
}