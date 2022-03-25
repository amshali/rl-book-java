package sutton.barto.rlbook;

import java.util.Vector;

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
