package sutton.barto.rlbook.chapter01.tictactoe;

public class Judge {
  private final IPlayer p1;
  private final IPlayer p2;
  private final boolean feedback;
  private State currentState;
  private IPlayer currentPlayer;

  public Judge(IPlayer p1, IPlayer p2, boolean feedback) {
    this.p1 = p1;
    this.p2 = p2;
    this.feedback = feedback;
    currentPlayer = null;
    currentState = State.init();
  }

  public void giveReward() {
    if (currentState.winner() == Game.P1_SYMBOL) {
      p1.feedReward(1);
      p2.feedReward(0);
    } else if (currentState.winner() == Game.P2_SYMBOL) {
      p1.feedReward(0);
      p2.feedReward(1);
    } else {
      p1.feedReward(0.5);
      p2.feedReward(0.5);
    }
  }

  public void reset() {
    p1.reset();
    p2.reset();
    currentState = State.init();
    currentPlayer = null;
  }

  public void feedCurrentState() {
    p1.feedState(currentState);
    p2.feedState(currentState);
  }

  public int play(boolean show) {
    reset();
    feedCurrentState();
    currentPlayer = p1;
    while (true) {
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
      if (currentPlayer == p1) {
        currentPlayer = p2;
      } else {
        currentPlayer = p1;
      }
    }
  }
}
