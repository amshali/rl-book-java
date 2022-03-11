package sutton.barto.rlbook.chapter01.tictactoe;

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
}
