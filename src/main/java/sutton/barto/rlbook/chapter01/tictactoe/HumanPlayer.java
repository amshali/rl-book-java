package sutton.barto.rlbook.chapter01.tictactoe;

import java.io.File;
import java.util.Scanner;

public class HumanPlayer implements IPlayer {
  private final Scanner input = new Scanner(System.in);
  private final int symbol;
  private State currentState;

  public HumanPlayer(int symbol) {
    this.symbol = symbol;
  }

  @Override
  public void reset() {
  }

  @Override
  public int symbol() {
    return symbol;
  }

  @Override
  public void feedState(State state) {
    currentState = state;
  }

  @Override
  public void feedReward(double reward) {
  }

  @Override
  public Action takeAction() {
    while (true) {
      System.out.print("Input your position: ");
      if (!input.hasNextInt()) {
        System.out.println("Invalid selection!");
        input.next();
        continue;
      }
      int pos = input.nextInt();
      if (pos >= 1 && pos <= 9) {
        pos -= 1;
        Position position = new Position(pos / Game.BOARD_ROWS, pos % Game.BOARD_COLS);
        if (currentState.data(position) != 0) {
          System.out.println("Invalid selection!");
          continue;
        }
        return new Action(position, symbol);
      } else {
        System.out.println("Invalid selection!");
      }
    }
  }

  @Override
  public void savePolicy(File file) {
  }

  @Override
  public void loadPolicy(File file) {
  }
}
