package sutton.barto.rlbook;

import java.util.*;
import java.util.stream.IntStream;

public class Utils {
  public static int argmax(double[] a) {
    double max = Double.NEGATIVE_INFINITY;
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
    double min = Double.POSITIVE_INFINITY;
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
    Vector<Integer> current = new Vector<>(dims.size());
    dims.forEach(d -> {
      if (d < 1) {
        throw new RuntimeException("Invalid dimension value " + d + " for permutation. Must be > " +
            "0.");
      }
      current.add(0);
    });
    List<Vector<Integer>> results = new ArrayList<>();
    results.add(new Vector<>(current));
    while (true) {
      for (int i = 0; i < dims.size(); i++) {
        current.set(i, (current.get(i) + 1) % dims.get(i));
        if (current.get(i) != 0) {
          break;
        }
      }
      Optional<Integer> sum = current.stream().reduce(Integer::sum);
      if (sum.isPresent() && sum.get() == 0) {
        break;
      }
      results.add(new Vector<>(current));
     }
    return results;
  }

  public static <T> Vector<T> vectorOf(T... data) {
    Vector<T> v = new Vector<>(data.length);
    Collections.addAll(v, data);
    return v;
  }

  public static <T> Vector<T> vectorOf(int size, T init) {
    Vector<T> v = new Vector<>(size);
    IntStream.range(0, size).forEach(i -> v.add(init));
    return v;
  }

  public static void main(String[] args) {
    Vector<Integer> dims = new Vector<>();
    dims.add(3);
    dims.add(4);
    dims.add(3);
    List<Vector<Integer>> perms = permutations(dims);
    perms.forEach(System.out::println);
  }
}
