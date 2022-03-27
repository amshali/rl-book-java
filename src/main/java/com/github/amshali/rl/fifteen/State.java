package com.github.amshali.rl.fifteen;

import java.util.*;

public class State {
  public static final int NIL_VALUE = 16;
  public static final State TERMINAL = terminalState();
  private final Random random = new Random();
  private final int[] numbers;
  private final List<Integer> possibleActions = new ArrayList<>();
  private final String hash;
  private boolean isTerminal = false;
  private int nilIndex;
  private Double value;
  
  public State(int[] numbers) {
    value = -random.nextDouble();
    this.numbers = numbers;
    var oneIndex = 0;
    nilIndex = 0;
    var bits = new ArrayList<Integer>();
    for (int i = 1; i < numbers.length; i++) {
      bits.add((numbers[i] - numbers[i - 1]) > 0 ? 1 : 0);
      if (numbers[i] == NIL_VALUE) {
        nilIndex = i;
      }
      if (numbers[i] == 1) {
        oneIndex = i;
      }
    }
    for (int i = 0; i < 2; i++) {
      for (int j = 5; j < 7; j++) {
        bits.add((numbers[j + i * 4] - numbers[j + i * 4 - 5]) > 0 ? 1 : 0);
      }
    }
    if (bits.stream().reduce(0, Integer::sum) == bits.size() && nilIndex == 15) {
      isTerminal = true;
      value = 0.0;
    }
    int code = 0;
    for (int i = 0; i < bits.size(); i++) {
      code += bits.get(i) * Math.pow(2, i);
    }
    hash = "%d,%d,%d".formatted(code, oneIndex, nilIndex);
    var i = nilIndex / 4;
    var j = nilIndex % 4;
    if (i - 1 >= 0) {
      possibleActions.add(4 * (i - 1) + j);
    }
    if (i + 1 < 4) {
      possibleActions.add(4 * (i + 1) + j);
    }
    if (j - 1 >= 0) {
      possibleActions.add(4 * i + j - 1);
    }
    if (j + 1 < 4) {
      possibleActions.add(4 * i + j + 1);
    }
  }

  public static State randomState() {
    var ints = new int[16];
    for (int i = 0; i < 16; i++) {
      ints[i] = i + 1;
    }
    var random = new Random();
    for (int i = 0; i < 16; i++) {
      var randomIndex = random.nextInt(16);
      var t = ints[i];
      ints[i] = ints[randomIndex];
      ints[randomIndex] = t;
    }
    return new State(ints);
  }

  private static State terminalState() {
    var ints = new int[16];
    for (int i = 0; i < 16; i++) {
      ints[i] = i + 1;
    }
    return new State(ints);
  }

  public static void main(String[] args) {
    var s = State.randomState();
    var random = new Random();
    var hashMap = new HashMap<String, State>();
    var matches = 0;
    System.out.println(State.TERMINAL);
    System.out.println(State.TERMINAL.hash());
    while (!Thread.interrupted()) {
      if (hashMap.size() % 5000 == 0) {
        System.out.println(">>>>>>>>>>>>" + hashMap.size());
        System.out.println("<<<<<<<<<<<<" + matches);
      }
      if (hashMap.containsKey(s.hash())) {
        var mapItem = hashMap.get(s.hash());
        if (!mapItem.toString().equals(s.toString())) {
          matches++;
//          System.out.println("Match!");
//          System.out.println(s);
//          System.out.println(s.hash());
//          System.out.println(mapItem);
//          System.out.println(mapItem.hash());
        }
      } else {
        hashMap.put(s.hash(), s);
      }
      if (s.isTerminal()) {
        System.exit(0);
      }
      s = s.nextState(s.possibleActions().get(random.nextInt(s.possibleActions().size())));
    }
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

  public State nextState(int action) {
    if (!possibleActions.contains(action)) {
      throw new RuntimeException("Invalid action: " + action + " in state: " +
          Arrays.toString(numbers));
    }
    int[] ints = Arrays.copyOf(numbers, numbers.length);
    ints[nilIndex] = numbers[action];
    ints[action] = NIL_VALUE;
    return new State(ints);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < numbers.length; i++) {
      if (i % 4 == 0) {
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
