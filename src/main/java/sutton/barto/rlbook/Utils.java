package sutton.barto.rlbook;

public class Utils {
  public static int argmax(double[] a) {
    double max = Double.MIN_VALUE;
    int maxIndex = -1;
    for (int i = 0; i < a.length; i++) {
      if (a[i] > max) {
        maxIndex = i;
        max = a[i];
      }
    }
    return maxIndex;
  }

  public static int argmin(double[] a) {
    double min = Double.MAX_VALUE;
    int minIndex = -1;
    for (int i = 0; i < a.length; i++) {
      if (a[i] < min) {
        minIndex = i;
        min = a[i];
      }
    }
    return minIndex;
  }
}
