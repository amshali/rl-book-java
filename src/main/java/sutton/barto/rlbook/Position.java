package sutton.barto.rlbook;

import java.util.Objects;

/**
 * A position on the X-O board.
 */
public class Position {
  private final int row;
  private final int column;

  public Position(int row, int column) {
    this.row = row;
    this.column = column;
  }

  public int row() {
    return row;
  }

  public int column() {
    return column;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Position)) {
      return false;
    }
    Position position = (Position) o;
    return row == position.row && column == position.column;
  }

  @Override
  public int hashCode() {
    return Objects.hash(row, column);
  }
}
