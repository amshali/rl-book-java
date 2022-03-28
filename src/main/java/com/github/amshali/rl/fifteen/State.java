package com.github.amshali.rl.fifteen;

import sutton.barto.rlbook.Position;

import java.util.*;

public class State {
  public static final int NUM_CELLS = 9;
  public static final int NIL_VALUE = 9;
  public static final State TERMINAL = terminalState();
  public final Map<Integer, String> actionState = new HashMap<>();
  private final int[] numbers;
  private final List<Integer> possibleActions = new ArrayList<>();
  private final String hash;
  private boolean isTerminal;
  private int nilIndex;
  private Double value = 1.0;

  public State(int[] numbers) {
    this.numbers = numbers;
    isTerminal = true;
    for (int i = 0; i < this.numbers.length; i++) {
      if (this.numbers[i] != i + 1) {
        isTerminal = false;
      }
      if (this.numbers[i] == NIL_VALUE) {
        nilIndex = i;
      }
    }
    if (isTerminal) {
      value = 0.0;
    }
    hash = generateHash();
    int width = (int) Math.sqrt(NUM_CELLS);
    var i = nilIndex / width;
    var j = nilIndex % width;
    if (i - 1 >= 0) {
      possibleActions.add(width * (i - 1) + j);
    }
    if (i + 1 < width) {
      possibleActions.add(width * (i + 1) + j);
    }
    if (j - 1 >= 0) {
      possibleActions.add(width * i + j - 1);
    }
    if (j + 1 < width) {
      possibleActions.add(width * i + j + 1);
    }
  }

  private static State terminalState() {
    var ints = new int[NUM_CELLS];
    for (int i = 0; i < NUM_CELLS; i++) {
      ints[i] = i + 1;
    }
    return new State(ints);
  }

  public static State nextState(State s, int action) {
    if (!s.possibleActions().contains(action)) {
      throw new RuntimeException("Invalid action: " + action + " in state: " + s);
    }
    int[] ints = s.cloneNumbers();
    var actionCell = ints[action];
    ints[action] = State.NIL_VALUE;
    ints[s.nilIndex()] = actionCell;
    return new State(ints);
  }

  private Position pointOf(int i) {
    int x = i / NUM_CELLS;
    int y = i % NUM_CELLS;
    return new Position(x, y);
  }

  public Double dist() {
    var dist = 1.0;
    for (int i = 0; i < NUM_CELLS; i++) {
      var iP = pointOf(i);
      var nP = pointOf(numbers[i] - 1);
      dist += Math.abs(iP.row() - nP.row()) + Math.abs(iP.column() - nP.column());
    }
    return Math.log(dist);
  }

  public int nilIndex() {
    return nilIndex;
  }

  private String generateHash() {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (int i : numbers) {
      if (!first) {
        sb.append(",");
      }
      sb.append(i);
      first = false;
    }
    return sb.toString();
  }

  public Double value() {
    return value;
  }

  public void setValue(Double value) {
    if (isTerminal) {
      throw new RuntimeException("Cannot set value for terminal state.");
    }
    this.value = value;
  }

  public boolean isTerminal() {
    return isTerminal;
  }

  public String hash() {
    return hash;
  }

  public List<Integer> possibleActions() {
    return possibleActions;
  }

  public int[] cloneNumbers() {
    return Arrays.copyOf(numbers, numbers.length);
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
}
