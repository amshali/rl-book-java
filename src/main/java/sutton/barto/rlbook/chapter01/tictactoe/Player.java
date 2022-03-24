package sutton.barto.rlbook.chapter01.tictactoe;

import sutton.barto.rlbook.Position;
import sutton.barto.rlbook.Tuple;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static sutton.barto.rlbook.chapter01.tictactoe.Game.BOARD_COLS;
import static sutton.barto.rlbook.chapter01.tictactoe.Game.BOARD_ROWS;

public class Player implements IPlayer {
  private final double stepSize;
  private final double exploreRate;
  private final Map<Long, State> allStates;
  private final List<State> states = new ArrayList<>();
  private final Random random = new Random();
  private Map<Long, Double> estimates = new HashMap<>();
  private int symbol;

  public Player(int symbol, double stepSize, double exploreRate, Map<Long, State> allStates) {
    this.stepSize = stepSize;
    this.exploreRate = exploreRate;
    this.allStates = allStates;
    setSymbol(symbol);
  }

  @Override
  public void reset() {
    states.clear();
  }

  @Override
  public int symbol() {
    return symbol;
  }

  private void setSymbol(int symbol) {
    this.symbol = symbol;
    for (Map.Entry<Long, State> e : allStates.entrySet()) {
      State state = e.getValue();
      Long hash = e.getKey();
      if (state.end()) {
        if (state.winner() == symbol) {
          estimates.put(hash, 1.0);
        } else {
          estimates.put(hash, 0.0);
        }
      } else {
        estimates.put(hash, 0.5);
      }
    }
  }

  @Override
  public void feedState(State state) {
    states.add(state);
  }

  @Override
  public void feedReward(double reward) {
    if (states.isEmpty()) {
      return;
    }
    ListIterator<State> it = states.listIterator(states.size());
    Double target = reward;
    while (it.hasPrevious()) {
      // NewEstimate = OldEstimate + StepSize * (Target - OldEstimate)
      Long latestStateHash = it.previous().hash();
      Double estimateValue = estimates.get(latestStateHash);
      Double value = estimateValue + stepSize * (target - estimateValue);
      estimates.put(latestStateHash, value);
    }
    states.clear();
  }

  private List<Position> validPositions(State state) {
    List<Position> positions = new ArrayList<>();
    for (int i = 0; i < BOARD_ROWS; i++) {
      for (int j = 0; j < BOARD_COLS; j++) {
        Position p = new Position(i, j);
        if (state.data(p) == 0) {
          positions.add(p);
        }
      }
    }
    return positions;
  }

  @Override
  public Action takeAction() {
    State state = states.get(states.size() - 1);
    List<State> nextStates = new ArrayList<>();
    List<Position> nextPositions = validPositions(state);
    Collections.shuffle(nextPositions);
    nextPositions.forEach((p -> nextStates.add(state.nextState(p, symbol))));
    // Exploring the state space: with the exploratory rate, we try to randomly choose
    // the next state sometimes.
    if (random.nextDouble() < exploreRate) {
      return new Action(nextPositions.get(0), this.symbol);
    }
    // Find the next state with the highest value:
    List<Tuple<Double, Position>> valuesPositions =
        IntStream.range(0, Math.min(nextPositions.size(), nextStates.size())).mapToObj(i ->
            new Tuple<>(estimates.get(nextStates.get(i).hash()), nextPositions.get(i))
        ).sorted((t0, t1) -> t1.first().compareTo(t0.first())).collect(Collectors.toList());
    return new Action(valuesPositions.get(0).second(), this.symbol);
  }

  @Override
  public void savePolicy(File file) throws IOException {
    FileOutputStream fos = new FileOutputStream(file);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(estimates);
    oos.close();
  }

  @Override
  public void loadPolicy(File file) throws IOException, ClassNotFoundException {
    FileInputStream fis = new FileInputStream(file);
    ObjectInputStream ois = new ObjectInputStream(fis);
    this.estimates = (Map<Long, Double>) ois.readObject();
  }
}
