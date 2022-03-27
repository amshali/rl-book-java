package sutton.barto.rlbook;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.*;
import java.util.stream.IntStream;

public class Utils {
  private static Random random = new Random();

  public static int argmax(double[] a) {
    var max = Double.NEGATIVE_INFINITY;
    int maxIndex = -1;
    for (int i = 0; i < a.length; i++) {
      if (a[i] > max) {
        maxIndex = i;
        max = a[i];
      } else if (a[i] == max && random.nextBoolean()) {
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

  public static List<Vector<Integer>> permutations(Vector<Integer> dims) {
    var current = new Vector<Integer>(dims.size());
    dims.forEach(d -> {
      if (d < 1) {
        throw new RuntimeException("Invalid dimension value " + d + " for permutation. Must be > " +
            "0.");
      }
      current.add(0);
    });
    var results = new ArrayList<Vector<Integer>>();
    results.add(new Vector<>(current));
    while (true) {
      for (int i = 0; i < dims.size(); i++) {
        current.set(i, (current.get(i) + 1) % dims.get(i));
        if (current.get(i) != 0) {
          break;
        }
      }
      var sum = current.stream().reduce(Integer::sum);
      if (sum.isPresent() && sum.get() == 0) {
        break;
      }
      results.add(new Vector<>(current));
    }
    return results;
  }

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

  public static void main(String[] args) {
    var dims = new Vector<Integer>();
    dims.add(3);
    dims.add(4);
    dims.add(3);
    var perms = permutations(dims);
    perms.forEach(System.out::println);
  }
}
