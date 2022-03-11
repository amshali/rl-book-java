package sutton.barto.rlbook.chapter01.tictactoe;

import java.io.File;
import java.util.Scanner;

public class HumanPlayer implements IPlayer {
  private int symbol;
  private State currentState;
  private final Scanner input = new Scanner(System.in);
  @Override
  public void reset() {
  }

  @Override
  public int symbol() {
    return symbol;
  }

  @Override
  public void setSymbol(int symbol) {
    this.symbol = symbol;
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
    System.out.print("Input your position: ");
    int pos = input.nextInt();
    pos -= 1;
    Position position = new Position(pos / Game.BOARD_ROWS, pos % Game.BOARD_COLS);
    if (currentState.data(position) != 0) {
      System.out.println("Invalid selection!");
      takeAction();
    }
    return new Action(position, symbol);
  }

  @Override
  public void savePolicy(File file) {
  }

  @Override
  public void loadPolicy(File file) {
  }
}
