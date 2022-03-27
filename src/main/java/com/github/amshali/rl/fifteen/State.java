package com.github.amshali.rl.fifteen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class State {
  public static final int NUM_CELLS = 9;
  public static final int NIL_VALUE = 9;
  public static final State TERMINAL = terminalState();
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

  public static State randomState() {
    var ints = new int[NUM_CELLS];
    for (int i = 0; i < NUM_CELLS; i++) {
      ints[i] = i + 1;
    }
    var random = new Random();
    for (int i = 0; i < NUM_CELLS; i++) {
      var randomIndex = random.nextInt(NUM_CELLS);
      var t = ints[i];
      ints[i] = ints[randomIndex];
      ints[randomIndex] = t;
    }
    return new State(ints);
  }

  private static State terminalState() {
    var ints = new int[NUM_CELLS];
    for (int i = 0; i < NUM_CELLS; i++) {
      ints[i] = i + 1;
    }
    return new State(ints);
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
