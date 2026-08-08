public class ReverseSorted {
    public static void main(String[] args) {
        int[] arr = {5, 4, 3, 2, 1};
        int swaps = 0;
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] > arr[i+1]) {
                int temp = arr[i];
                arr[i] = arr[i+1];
                arr[i+1] = temp;
                swaps++;
            }
        }
        System.out.println("Swaps: " + swaps);
    }
}