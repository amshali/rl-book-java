package com.github.amshali.rl.fifteen;

import sutton.barto.rlbook.Utils;

import java.util.*;

public class FifteenState {
  public static final int NUM_CELLS = 16;
  public static final int NIL_VALUE = 16;
  public static FifteenState ONE_TO_FOUR_SOLVED_STATE = new FifteenState(
      new Integer[]{1, 2, 3, 4, NIL_VALUE, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
  public static FifteenState FIVE_TO_EIGHT_SOLVED_STATE = new FifteenState(
      new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, NIL_VALUE, -1, -1, -1, -1, -1, -1, -1});
  public static FifteenState SOLVED_STATE = new FifteenState(
      new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, NIL_VALUE});
  /**
   * Map of action to next states for this state.
   */
  private final Map<Integer, String> actionState = new HashMap<>();
  private final Integer[] numbers;
  private final String hash;
  private final int rowsSolved;
  private FifteenPuzzleEpisode episode;
  private int nilIndex;
  private Double value = 0.0;

  public FifteenState(Integer[] numbers) {
    if (numbers.length != NUM_CELLS) {
      throw new RuntimeException("Invalid size of numbers for state");
    }
    this.numbers = numbers;
    rowsSolved = countRowsSolved(numbers);
    episode = FifteenPuzzleEpisode.ONE_TO_FOUR;
    if (rowsSolved == 1) {
      episode = FifteenPuzzleEpisode.FIVE_TO_EIGHT;
    } else if (rowsSolved == 2) {
      episode = FifteenPuzzleEpisode.NINE_TO_FIFTEEN;
    } else if (rowsSolved == 4) {
      episode = FifteenPuzzleEpisode.SOLVED;
    }
    hash = generateHash(this.numbers, episode);
    for (int i = 0; i < this.numbers.length; i++) {
      if (this.numbers[i] == NIL_VALUE) {
        nilIndex = i;
      }
    }
  }

  public static int countRowsSolved(Integer[] numbers) {
    for (int i = 0; i < NUM_CELLS - 1; i++) {
      if (numbers[i] != i + 1) {
        return i / 4;
      }
    }
    return (int) Math.sqrt(NUM_CELLS);
  }

  public static int countMasked(Integer[] numbers) {
    int c = 0;
    for (Integer number : numbers) {
      if (number == -1) {
        c++;
      }
    }
    return c;
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

  public static String generateHash(Integer[] numbers, FifteenPuzzleEpisode episode) {
    return switch (episode) {
      case ONE_TO_FOUR -> Utils.join(
          Arrays.stream(mask(numbers, 1, 4)).toArray(), ",");
      case FIVE_TO_EIGHT -> Utils.join(
          Arrays.stream(mask(numbers, 1, 8)).toArray(), ",");
      case NINE_TO_FIFTEEN, SOLVED -> Utils.join(
          Arrays.stream(mask(numbers, 1, 15)).toArray(), ",");
    };
  }

  public FifteenState nextState(int action) {
    Integer[] ints = Arrays.copyOf(numbers, numbers.length);
    var actionCell = ints[action];
    ints[action] = NIL_VALUE;
    ints[nilIndex] = actionCell;
    return new FifteenState(ints);
  }

  public String hash() {
    return hash;
  }

  public boolean isTerminal() {
    return episode == FifteenPuzzleEpisode.SOLVED ||
        (rowsSolved == 1 && countMasked(numbers) == 11) ||
        (rowsSolved == 2 && countMasked(numbers) == 7);
  }

  public boolean isSolved() {
    return episode == FifteenPuzzleEpisode.SOLVED;
  }

  public List<Integer> actionsOf(FifteenPuzzleEpisode episode) {
    final List<Integer> possibleActions = new ArrayList<>();
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
    return possibleActions;
  }

  public List<Integer> possibleActions() {
    return actionsOf(FifteenPuzzleEpisode.SOLVED);
  }

  public List<Integer> goodActions() {
    return actionsOf(episode);
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

  public int rowsSolved() {
    return rowsSolved;
  }
}
