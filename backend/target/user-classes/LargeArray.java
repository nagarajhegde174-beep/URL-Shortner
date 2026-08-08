public class LargeArray {
    public static void main(String[] args) {
        int[] arr = new int[100];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i;
        }
        System.out.println("Large array initialized: " + arr.length);
    }
}