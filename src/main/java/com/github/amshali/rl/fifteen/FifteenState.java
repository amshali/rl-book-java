package com.github.amshali.rl.fifteen;

import sutton.barto.rlbook.Utils;

import java.util.Arrays;

public class FifteenState {
  public static final int NUM_CELLS = 16;
  public static final int NIL_VALUE = 16;

  private final Integer[] numbers = new Integer[NUM_CELLS];

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
          Arrays.stream(mask(numbers, 5, 8)).toArray(), ",");
      case NINE_TO_FIFTEEN -> Utils.join(
          Arrays.stream(mask(numbers, 9, 15)).toArray(), ",");
    };
  }

  public static void main(String[] args) {
    System.out.println(
        generateHash(new Integer[]{2, 16, 11, 3, 9, 10, 13, 12, 4, 1, 15, 14, 7, 8, 5, 6},
            FifteenPuzzleEpisode.ONE_TO_FOUR));
    System.out.println(
        generateHash(new Integer[]{2, 16, 11, 3, 9, 10, 13, 12, 4, 1, 15, 14, 7, 8, 5, 6},
            FifteenPuzzleEpisode.FIVE_TO_EIGHT));
    System.out.println(
        generateHash(new Integer[]{2, 16, 11, 3, 9, 10, 13, 12, 4, 1, 15, 14, 7, 8, 5, 6},
            FifteenPuzzleEpisode.NINE_TO_FIFTEEN));
  }
}
