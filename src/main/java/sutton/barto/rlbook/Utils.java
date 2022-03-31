package sutton.barto.rlbook;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.*;
import java.util.stream.IntStream;

public class Utils {
  private static final Random random = new Random();

  public static int argmax(double[] a) {
    return argmax(Arrays.stream(a).boxed().toArray(Double[]::new));
  }

  public static int argmax(Double[] a) {
    var max = Double.NEGATIVE_INFINITY;
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
    var min = Double.POSITIVE_INFINITY;
    int minIndex = -1;
    for (int i = 0; i < a.length; i++) {
      if (a[i] < min) {
        minIndex = i;
        min = a[i];
      }
    }
    return minIndex;
  }

  @SafeVarargs
  public static <T> Vector<T> vectorOf(T... data) {
    var v = new Vector<T>(data.length);
    Collections.addAll(v, data);
    return v;
  }

  public static <T> Vector<T> vectorOf(int size, T init) {
    var v = new Vector<T>(size);
    IntStream.range(0, size).forEach(i -> v.add(init));
    return v;
  }

  public static RealMatrix zeros(int rows, int columns) {
    var matrix = MatrixUtils.createRealMatrix(rows, columns);
    for (int i = 0; i < matrix.getRowDimension(); i++) {
      for (int j = 0; j < matrix.getColumnDimension(); j++) {
        matrix.setEntry(i, j, 0.0);
      }
    }
    return matrix;
  }

  public static Set<int[]> binaryCombinations(int zeros, int ones) {
    var result = new HashSet<int[]>();
    int bits = zeros + ones;
    if (bits > 32 || bits < 1) {
      throw new RuntimeException("Invalid arguments");
    }
    int bound = (int) Math.pow(2, bits);
    for (int i = 0; i < bound; i++) {
      var b = generateBitVector(i, bits);
      if (ones == Arrays.stream(b).sum()) {
        result.add(b);
      }
    }
    return result;
  }

  public static String join(Object[] vs, String delimiter) {
    boolean first = true;
    StringBuilder sb = new StringBuilder();
    for (Object v : vs) {
      if (!first) {
        sb.append(delimiter);
      }
      sb.append(v);
      first = false;
    }
    return sb.toString();
  }

  /**
   * Make a random choice from the given the probabilities.
   *
   * @param probabilities array of probabilities. The sum of values in this array must be 1.
   * @return the index of the chosen item.
   */
  public static int choice(double[] probabilities) {
    var rnd = random.nextDouble();
    var sumProb = 0.0;
    for (int i = 0; i < probabilities.length; i++) {
      sumProb += probabilities[i];
      if (rnd <= sumProb) {
        return i;
      }
    }
    throw new RuntimeException("Could not choose a value with given probabilities.");
  }

  public static int[] generateBitVector(int n, int bits) {
    var r = new int[bits];
    for (int i = 0; i < bits; i++) {
      r[bits - i - 1] = n % 2;
      n /= 2;
    }
    return r;
  }

  public static void main(String[] args) {
    binaryCombinations(5, 8).forEach(r -> System.out.println(Arrays.toString(r)));
  }
}
