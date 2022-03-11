package sutton.barto.rlbook.chapter01.tictactoe;

import java.io.File;
import java.io.IOException;

public interface IPlayer {
  void reset();

  int symbol();

  void setSymbol(int symbol);

  void feedState(State state);

  void feedReward(double reward);

  Action takeAction();

  void savePolicy(File file) throws IOException;
  void loadPolicy(File file) throws IOException, ClassNotFoundException;
}
