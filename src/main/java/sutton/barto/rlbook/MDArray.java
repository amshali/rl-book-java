package sutton.barto.rlbook;

import java.util.Arrays;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

public class MDArray {
  private final Integer[] dimensions;
  private final Integer[] skips;
  private final Vector<Number> data = new Vector<>();

  public MDArray(Integer[] dimensions) {
    for (Integer dimension : dimensions) {
      if (dimension < 1 || dimension > 10000) {
        throw new IllegalArgumentException("Invalid dimension argument");
      }
    }
    this.dimensions = dimensions;
    int size = Arrays.stream(this.dimensions).reduce(1, (i, j) -> i * j);
    data.setSize(size);
    skips = new Integer[dimensions.length];
    skips[dimensions.length - 1] = 1;
    for (int i = dimensions.length - 2; i >= 0; i--) {
      skips[i] = skips[i + 1] * dimensions[i + 1];
    }
  }

  public MDArray(Number init, Integer[] dimensions) {
    this(dimensions);
    IntStream.range(0, data.size()).forEach(i -> data.set(i, init));
  }

  private MDArray(Vector<? extends Number> initData, Integer[] dimensions) {
    this(dimensions);
    if (data.size() != initData.size()) {
      throw new IllegalArgumentException("Invalid initData size");
    }
    IntStream.range(0, data.size()).forEach(i -> data.set(i, initData.get(i)));
  }

  public Integer[] dimensions() {
    return dimensions;
  }

  public MDArray op(BiFunction<Number, Number, Number> f, MDArray other) {
    var result = new Vector<Number>(data.size());
    for (int i = 0; i < data.size(); i++) {
      result.add(i, f.apply(data.get(i), other.data.get(i)));
    }
    return new MDArray(result, this.dimensions());
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

  public void set(Number datum, Integer... coordinates) {
    data.set(getIndex(coordinates), datum);
  }

  public void set(Function<Number, Number> f, Integer... coordinates) {
    int index = getIndex(coordinates);
    data.set(index, f.apply(data.get(index)));
  }

  private Integer getIndex(Integer[] coordinates) {
    checkCoordinates(coordinates);
    int index = 0;
    for (int i = 0; i < coordinates.length; i++) {
      index += coordinates[i] * skips[i];
    }
    return index;
  }

  public Number get(Integer... coordinates) {
    return data.get(getIndex(coordinates));
  }
}
