package sutton.barto.rlbook.chapter01.tictactoe;

/**
 * This class abstracts the action that took place. A player put its symbol in a position.
 */
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
