package sutton.barto.rlbook;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MultiDimArray<T> {

  private final Integer[] dimensions;
  private final Integer[] skips;
  private final Vector<T> data = new Vector<>();

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
//    var m = new MultiDimArray<Double>(new Integer[]{3, 2, 2});
//    m.set(new Integer[]{1, 1, 0}, 1.2);
//    m.set(new Integer[]{0, 0, 1}, -1.3);
//    m.set(new Integer[]{2, 1, 1}, 2.3);
//    m.stream().forEach(System.out::println);
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
