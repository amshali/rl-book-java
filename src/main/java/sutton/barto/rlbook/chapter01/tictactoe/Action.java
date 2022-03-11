package sutton.barto.rlbook.chapter01.tictactoe;

public class Action {
  private final Position position;
  private final int symbol;

  public Action(Position position, int symbol) {
    this.position = position;
    this.symbol = symbol;
  }

  public Position position() {
    return position;
  }

  public int symbol() {
    return symbol;
  }
}
