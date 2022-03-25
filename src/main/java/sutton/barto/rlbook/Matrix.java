package sutton.barto.rlbook;

import java.util.Vector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Matrix<T> {

  private int rows, columns;
  private Vector<Vector<T>> data;

  public Matrix(int rows, int columns, T init) {
    this.rows = rows;
    this.columns = columns;
    data = new Vector<>();
    for (int i = 0; i < rows; i++) {
      data.addElement(new Vector<>());
      for (int j = 0; j < columns; j++) {
        data.get(i).addElement(init);
      }
    }
  }

  public Matrix(Matrix<T> o) {
    this.rows = o.rows;
    this.columns = o.columns;
    data = new Vector<>();
    for (int i = 0; i < rows; i++) {
      data.addElement(new Vector<>());
      for (int j = 0; j < columns; j++) {
        data.get(i).addElement(o.get(i, j));
      }
    }
  }

  public Stream<T> row(int i) {
    return data.get(i).stream();
  }

  public Stream<T> column(int c) {
    return IntStream.range(0, rows()).mapToObj(i -> get(i, c));
  }

  public T get(int i, int j) {
    return data.get(i).get(j);
  }

  public T set(int i, int j, T d) {
    var current = data.get(i).get(j);
    data.get(i).set(j, d);
    return current;
  }

  public int rows() {
    return rows;
  }

  public int columns() {
    return columns;
  }
}
