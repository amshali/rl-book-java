package com.github.amshali.rl.fifteen;

import sutton.barto.rlbook.Utils;

import java.util.*;

public class FifteenState {
  public static final int NUM_CELLS = 16;
  public static final int NIL_VALUE = 16;
  /**
   * Set of terminal states.
   */
  public static Map<FifteenPuzzleEpisode, FifteenState> terminalStates =
      Map.of(FifteenPuzzleEpisode.ONE_TO_FOUR, terminalStateOf(FifteenPuzzleEpisode.ONE_TO_FOUR),
          FifteenPuzzleEpisode.FIVE_TO_EIGHT, terminalStateOf(FifteenPuzzleEpisode.FIVE_TO_EIGHT),
          FifteenPuzzleEpisode.NINE_TO_FIFTEEN,
          terminalStateOf(FifteenPuzzleEpisode.NINE_TO_FIFTEEN),
          FifteenPuzzleEpisode.ONE_TO_FIFTEEN,
          terminalStateOf(FifteenPuzzleEpisode.ONE_TO_FIFTEEN));
  /**
   * Map of action to next states for this state.
   */
  private final Map<Integer, String> actionState = new HashMap<>();
  private final Integer[] numbers;
  private final String hash;
  private final FifteenPuzzleEpisode episode;
  private final List<Integer> possibleActions = new ArrayList<>();
  private int nilIndex;
  private Double value = 1.0;

  public FifteenState(Integer[] numbers, FifteenPuzzleEpisode episode) {
    if (numbers.length != NUM_CELLS) {
      throw new RuntimeException("Invalid size of numbers for state");
    }
    this.episode = episode;
    this.numbers = numbers;
    hash = generateHash(this.numbers, episode);
    for (int i = 0; i < this.numbers.length; i++) {
      if (this.numbers[i] == NIL_VALUE) {
        nilIndex = i;
      }
    }
    int width = (int) Math.sqrt(NUM_CELLS);
    var row = nilIndex / width;
    var col = nilIndex % width;
    var rowBound = 0;
    switch (episode) {
      case FIVE_TO_EIGHT -> rowBound = 1;
      case NINE_TO_FIFTEEN -> rowBound = 2;
    }
    if (row - 1 >= rowBound) {
      possibleActions.add(width * (row - 1) + col);
    }
    if (row + 1 < width) {
      possibleActions.add(width * (row + 1) + col);
    }
    if (col - 1 >= 0) {
      possibleActions.add(width * row + col - 1);
    }
    if (col + 1 < width) {
      possibleActions.add(width * row + col + 1);
    }
  }

  public static Integer[] mask(Integer[] numbers, int lower, int upper) {
    var r = new Integer[numbers.length];
    for (int i = 0; i < numbers.length; i++) {
      if ((numbers[i] < lower || numbers[i] > upper) && numbers[i] != NIL_VALUE) {
        r[i] = -1;
      } else {
        r[i] = numbers[i];
      }
    }
    return r;
  }

  public static FifteenState terminalStateOf(FifteenPuzzleEpisode episode) {
    return switch (episode) {
      case ONE_TO_FOUR -> new FifteenState(
          new Integer[]{1, 2, 3, 4, NIL_VALUE, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
          FifteenPuzzleEpisode.ONE_TO_FOUR);
      case FIVE_TO_EIGHT -> new FifteenState(
          new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, NIL_VALUE, -1, -1, -1, -1, -1, -1, -1},
          FifteenPuzzleEpisode.FIVE_TO_EIGHT);
      case NINE_TO_FIFTEEN -> new FifteenState(
          new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, NIL_VALUE},
          FifteenPuzzleEpisode.NINE_TO_FIFTEEN);
      case ONE_TO_FIFTEEN -> new FifteenState(
          new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, NIL_VALUE},
          FifteenPuzzleEpisode.ONE_TO_FIFTEEN);
    };
  }

  public static String generateHash(Integer[] numbers, FifteenPuzzleEpisode episode) {
    return switch (episode) {
      case ONE_TO_FOUR -> Utils.join(
          Arrays.stream(mask(numbers, 1, 4)).toArray(), ",");
      case FIVE_TO_EIGHT -> Utils.join(
          Arrays.stream(mask(numbers, 1, 8)).toArray(), ",");
      case NINE_TO_FIFTEEN, ONE_TO_FIFTEEN -> Utils.join(
          Arrays.stream(mask(numbers, 1, 15)).toArray(), ",");
    };
  }

  public static void main(String[] args) {
    var a = terminalStates.get(FifteenPuzzleEpisode.ONE_TO_FOUR);
    var rnd = new Random();
    while (!Thread.interrupted()) {
      System.out.println(a);
      System.out.println("=================");
      var action = a.possibleActions.get(rnd.nextInt(a.possibleActions.size()));
      a = a.nextState(action);
      try {
        Thread.sleep(100);
      } catch (InterruptedException ignored) {
      }
    }
  }

  public Integer[] numbers() {
    return numbers;
  }

  public FifteenState nextState(int action) {
    if (!possibleActions().contains(action)) {
      throw new RuntimeException("Invalid action: " + action + " in state:\n" + this);
    }
    Integer[] ints = Arrays.copyOf(numbers, numbers.length);
    var actionCell = ints[action];
    ints[action] = NIL_VALUE;
    ints[nilIndex] = actionCell;
    var ns = new FifteenState(ints, episode);
    if (terminalStates.get(episode).hash().equals(ns.hash())) {
      return terminalStates.get(episode);
    }
    return ns;
  }

  public String hash() {
    return hash;
  }

  public boolean isTerminal() {
    for (var s : terminalStates.values()) {
      if (s.hash.equals(hash)) {
        return true;
      }
    }
    return false;
  }

  public List<Integer> possibleActions() {
    return possibleActions;
  }

  public Double value() {
    return value;
  }

  public void setValue(Double value) {
    if (isTerminal()) {
      throw new RuntimeException("Cannot set value for terminal state.");
    }
    this.value = value;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    int width = (int) Math.sqrt(NUM_CELLS);
    for (int i = 0; i < numbers.length; i++) {
      if (i % width == 0) {
        sb.append("\n|");
      }
      if (numbers[i] == NIL_VALUE) {
        sb.append("   |");
      } else {
        sb.append("%3d|".formatted(numbers[i]));
      }
    }
    return sb.toString().trim();
  }

  public Map<Integer, String> actionState() {
    return actionState;
  }
}
