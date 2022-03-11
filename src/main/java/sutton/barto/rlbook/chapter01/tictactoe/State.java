package sutton.barto.rlbook.chapter01.tictactoe;

import sutton.barto.rlbook.ConsoleColors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class State {

  private final int[][] board = new int[Game.BOARD_ROWS][Game.BOARD_COLS];
  private int winner = 0;
  private Long hash = null;
  private Boolean end = null;
  private Boolean init = true;
  private Integer justPlayedSymbol;

  private State() {
    for (int i = 0; i < Game.BOARD_ROWS; i++) {
      Arrays.fill(board[i], 0);
    }
  }

  public static State init() {
    return new State();
  }

  public boolean end() {
    if (end == null) {
      end = false;
      List<Integer> results = new ArrayList<>();
      for (int i = 0; i < Game.BOARD_ROWS; i++) {
        results.add(Arrays.stream(board[i]).sum());
      }
      for (int i = 0; i < Game.BOARD_COLS; i++) {
        int finalI = i;
        results.add(IntStream.range(0, Game.BOARD_ROWS).map(j -> board[j][finalI]).sum());
      }
      results.add(IntStream.range(0, Game.BOARD_ROWS).map(i -> board[i][i]).sum());
      results.add(
          IntStream.range(0, Game.BOARD_ROWS).map(i -> board[i][Game.BOARD_ROWS - 1 - i]).sum());
      for (int result : results) {
        if (result == 3) {
          winner = Game.P1_SYMBOL;
          end = true;
        }
        if (result == -3) {
          winner = Game.P2_SYMBOL;
          end = true;
        }
      }
      int sum = 0;
      for (int i = 0; i < Game.BOARD_ROWS; i++) {
        for (int j = 0; j < Game.BOARD_COLS; j++) {
          sum += Math.abs(board[i][j]);
        }
      }
      // Tie:
      if (sum == Game.BOARD_COLS * Game.BOARD_ROWS) {
        winner = 0;
        end = true;
      }
    }
    return end;
  }

  public void printBoard() {
    for (int i = 0; i < Game.BOARD_ROWS; i++) {
      System.out.println("-------------");
      StringBuilder sb = new StringBuilder("| ");
      for (int j = 0; j < Game.BOARD_COLS; j++) {
        if (board[i][j] == Game.P1_SYMBOL) {
          sb.append("O");
        }
        if (board[i][j] == 0) {
          sb.append(ConsoleColors.GREEN_UNDERLINED);
          sb.append(i * 3 + j + 1);
          sb.append(ConsoleColors.RESET);
        }
        if (board[i][j] == Game.P2_SYMBOL) {
          sb.append("X");
        }
        sb.append(" | ");
      }
      System.out.println(sb);
    }
    System.out.println("-------------");
  }

  public int winner() {
    return winner;
  }

  public Long hash() {
    if (init) {
      hash = 0L;
    }
    if (hash == null) {
      long h = 0L;
      for (int i = 0; i < Game.BOARD_ROWS; i++) {
        for (int j = 0; j < Game.BOARD_COLS; j++) {
          if (board[i][j] == Game.P2_SYMBOL) {
            h = h * 3 + 2;
          } else {
            h = h * 3 + board[i][j];
          }
        }
      }
      hash = h * justPlayedSymbol;
    }
    return hash;
  }

  public int data(Position p) {
    return board[p.row()][p.column()];
  }

  public State nextState(Position p, int symbol) {
    State ns = new State();
    ns.init = false;
    ns.justPlayedSymbol = symbol;
    for (int i = 0; i < board.length; i++) {
      ns.board[i] = Arrays.copyOf(board[i], board[i].length);
    }
    ns.board[p.row()][p.column()] = symbol;
    return ns;
  }
}
