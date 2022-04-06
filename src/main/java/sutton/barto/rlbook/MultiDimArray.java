package sutton.barto.rlbook;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MultiDimArray<T> {

  private final Integer[] dimensions;
  private final Integer[] skips;
  private final Vector<T> data = new Vector<>();
  private List<Integer[]> coordinates;

  public MultiDimArray(T init, Integer... dimensions) {
    for (Integer dimension : dimensions) {
      if (dimension < 1 || dimension > 10000) {
        throw new RuntimeException("Invalid argument");
      }
    }
    this.dimensions = dimensions;
    skips = new Integer[dimensions.length];
    int size = Arrays.stream(this.dimensions).reduce(1, (i, j) -> i * j);
    data.setSize(size);
    IntStream.range(0, size).forEach(i -> data.set(i, init));
    for (int i = 0; i < dimensions.length - 1; i++) {
      skips[i] = 1;
      for (int i1 = i + 1; i1 < dimensions.length; i1++) {
        skips[i] *= dimensions[i1];
      }
    }
    skips[dimensions.length - 1] = 0;
    coordinates = new ArrayList<>();
    var current = startingCoordinate();
    coordinates.add(current);
    while (hasNextCoordinate(current)) {
      current = nextCoordinate(current);
      coordinates.add(current);
    }
  }

  public MultiDimArray(Vector<T> initData, Integer... dimensions) {
    for (Integer dimension : dimensions) {
      if (dimension < 1 || dimension > 10000) {
        throw new RuntimeException("Invalid argument");
      }
    }
    this.dimensions = dimensions;
    skips = new Integer[dimensions.length];
    int size = Arrays.stream(this.dimensions).reduce(1, (i, j) -> i * j);
    if (size != initData.size()) {
      throw new RuntimeException("Invalid initData");
    }
    this.data.setSize(size);
    for (int i = 0; i < initData.size(); i++) {
      this.data.set(i, initData.get(i));
    }
    for (int i = 0; i < dimensions.length - 1; i++) {
      skips[i] = 1;
      for (int i1 = i + 1; i1 < dimensions.length; i1++) {
        skips[i] *= dimensions[i1];
      }
    }
    skips[dimensions.length - 1] = 0;
  }

  public static void main(String[] args) {
    var m = new MultiDimArray<Double>(0.0, 3, 5, 4);
    var current = m.startingCoordinate();
    System.out.println(Arrays.toString(current));
    while (m.hasNextCoordinate(current)) {
      current = m.nextCoordinate(current);
      System.out.println(Arrays.toString(current));
    }
  }

  public Integer[] dimensions() {
    return dimensions;
  }

  public Stream<T> stream() {
    return data.stream();
  }

  private void checkCoordinates(Integer[] coordinates) {
    if (coordinates.length != dimensions.length) {
      throw new RuntimeException("Invalid coordinates");
    }
    for (int i = 0; i < coordinates.length; i++) {
      if (coordinates[i] < 0 || coordinates[i] >= dimensions[i]) {
        throw new RuntimeException("Invalid coordinates: " + Arrays.toString(coordinates));
      }
    }
  }

  public Integer[] startingCoordinate() {
    var r = new Integer[this.dimensions.length];
    Arrays.fill(r, 0);
    return r;
  }

  public Integer[] nextCoordinate(Integer[] current) {
    if (!hasNextCoordinate(current)) {
      throw new RuntimeException("Has no next coordinate: " + Arrays.toString(current));
    }
    Integer[] next = Arrays.copyOf(current, current.length);
    int lastI = next.length - 1;
    next[lastI] = (next[lastI] + 1) % dimensions[lastI];
    if (next[lastI] == 0) {
      int j = lastI - 1;
      while (j >= 0) {
        next[j] = (next[j] + 1) % dimensions[j];
        if (next[j] > 0) {
          break;
        }
        j--;
      }
    }
    return next;
  }

  public Boolean hasNextCoordinate(Integer[] current) {
    for (int i = 0; i < dimensions.length; i++) {
      if (current[i] < dimensions[i] - 1) {
        return true;
      }
    }
    return false;
  }

  public List<Integer[]> coordinates() {
    return coordinates;
  }

  public void set(T datum, Integer... coordinates) {
    data.set(getIndex(coordinates), datum);
  }

  public void getSet(Function<T, T> f, Integer... coordinates) {
    int index = getIndex(coordinates);
    data.set(index, f.apply(data.get(index)));
  }

  private Integer getIndex(Integer[] coordinates) {
    checkCoordinates(coordinates);
    Integer index = 0;
    for (int i = 0; i < coordinates.length - 1; i++) {
      index += coordinates[i] * skips[i];
    }
    index += coordinates[coordinates.length - 1];
    return index;
  }

  public T get(Integer... coordinates) {
    return data.get(getIndex(coordinates));
  }

  public int size() {
    return data.size();
  }

  public Iterator<T> iterator() {
    return data.iterator();
  }
}
