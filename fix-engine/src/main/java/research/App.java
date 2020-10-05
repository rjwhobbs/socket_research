package research;

class FixEngine {

  public static int sum(byte[] arr) {
    int sum = 0;

    for (int i = 0; i < arr.length; i++) {
      sum += arr[i];
    }

    return sum;
  }

  public int checkSum(String message) {

    return 0;
  }
}

public class App {
  public static void main(String[] args) {
    System.out.println(FixEngine.sum("ab".getBytes()));
  }
}
