package sutton.barto.rlbook.chapter01.tictactoe;

public class Judge {
  private State currentState;
  private final IPlayer p1;
  private final IPlayer p2;
  private IPlayer currentPlayer;
  public final int P1_SYMBOL = 1;
  public final int P2_SYMBOL = -1;
  private final boolean feedback;

  public Judge(IPlayer p1, IPlayer p2, boolean feedback) {
    this.p1 = p1;
    this.p2 = p2;
    this.feedback = feedback;
    currentPlayer = null;
    this.p1.setSymbol(P1_SYMBOL);
    this.p2.setSymbol(P2_SYMBOL);
    currentState = new State();
  }

  public void giveReward() {
    if (currentState.winner() == P1_SYMBOL) {
      p1.feedReward(1);
      p2.feedReward(0);
    } else if (currentState.winner() == P2_SYMBOL) {
      p1.feedReward(0);
      p2.feedReward(1);
    } else {
      // Why?
      p1.feedReward(0.1);
      p2.feedReward(0.5);
    }
  }

  public void reset() {
    p1.reset();
    p2.reset();
    currentState = new State();
    currentPlayer = null;
  }

  public void feedCurrentState() {
    p1.feedState(currentState);
    p2.feedState(currentState);
  }

  public int play(boolean show) {
    reset();
    feedCurrentState();
    while (true) {
      if (currentPlayer == p1) {
        currentPlayer = p2;
      } else {
        currentPlayer = p1;
      }
      if (show) {
        currentState.printBoard();
      }
      Action action = currentPlayer.takeAction();
      currentState = currentState.nextState(action.position(), action.symbol());
      feedCurrentState();
      if (currentState.end()) {
        if (feedback) {
          giveReward();
        }
        if (show) {
          currentState.printBoard();
        }
        return currentState.winner();
      }
    }
  }
}
