package sutton.barto.rlbook;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Matrix<T> {

  private final int rows;
  private final int columns;
  private final T[][] data;

  @SuppressWarnings("unchecked")
  public Matrix(int rows, int columns, T init) {
    this.rows = rows;
    this.columns = columns;
    data = (T[][]) new Object[rows][];
    for (int i = 0; i < rows; i++) {
      data[i] = (T[]) new Object[columns];
      for (int j = 0; j < columns; j++) {
        data[i][j] = init;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public Matrix(Matrix<T> o) {
    this.rows = o.rows;
    this.columns = o.columns;
    data = (T[][]) new Object[rows][];
    for (int i = 0; i < rows; i++) {
      data[i] = (T[]) new Object[columns];
      for (int j = 0; j < columns; j++) {
        data[i][j] = o.get(i, j);
      }
    }
  }

  public Stream<T> row(int i) {
    return Arrays.stream(data[i]);
  }

  public Stream<T> column(int c) {
    return IntStream.range(0, rows()).mapToObj(i -> get(i, c));
  }

  public T get(int i, int j) {
    return data[i][j];
  }

  public void set(int i, int j, T d) {
    data[i][j] = d;
  }

  public int rows() {
    return rows;
  }

  public int columns() {
    return columns;
  }
}
