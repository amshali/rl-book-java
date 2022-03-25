package sutton.barto.rlbook.chapter01.tictactoe;

import sutton.barto.rlbook.ConsoleColors;
import sutton.barto.rlbook.Matrix;
import sutton.barto.rlbook.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class State {

  private Matrix<Integer> board = new Matrix<>(Game.BOARD_ROWS, Game.BOARD_COLS, 0);
  private int winner = 0;
  private Long hash = null;
  private Boolean end = null;
  private Boolean init = true;
  private Integer justPlayedSymbol;

  private State() {
  }

  public static State init() {
    return new State();
  }

  public boolean end() {
    if (end == null) {
      end = false;
      List<Integer> results = new ArrayList<>();
      for (int i = 0; i < Game.BOARD_ROWS; i++) {
        results.add(board.row(i).reduce(0, Integer::sum));
      }
      for (int i = 0; i < Game.BOARD_COLS; i++) {
        results.add(board.column(i).reduce(9, Integer::sum));
      }
      results.add(IntStream.range(0, Game.BOARD_ROWS).map(i -> board.get(i, i)).sum());
      results.add(
          IntStream.range(0, Game.BOARD_ROWS).map(i -> board.get(i, Game.BOARD_ROWS - 1 - i))
              .sum());
      for (int result : results) {
        if (result == 3) {
          winner = Game.P1_SYMBOL;
          end = true;
          return true;
        }
        if (result == -3) {
          winner = Game.P2_SYMBOL;
          end = true;
          return true;
        }
      }
      // Computing the tie:
      int sum = 0;
      for (int i = 0; i < Game.BOARD_ROWS; i++) {
        for (int j = 0; j < Game.BOARD_COLS; j++) {
          sum += Math.abs(board.get(i, j));
        }
      }
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
        if (board.get(i, j) == Game.P1_SYMBOL) {
          sb.append("O");
        }
        if (board.get(i, j) == 0) {
          sb.append(ConsoleColors.GREEN_UNDERLINED);
          sb.append(i * 3 + j + 1);
          sb.append(ConsoleColors.RESET);
        }
        if (board.get(i, j) == Game.P2_SYMBOL) {
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
          if (board.get(i, j) == Game.P2_SYMBOL) {
            h = h * 3 + 2;
          } else {
            h = h * 3 + board.get(i, j);
          }
        }
      }
      hash = h * justPlayedSymbol;
    }
    return hash;
  }

  public int data(Position p) {
    return board.get(p.row(), p.column());
  }

  public State nextState(Position p, int symbol) {
    State ns = new State();
    ns.init = false;
    ns.justPlayedSymbol = symbol;
    ns.board = new Matrix<>(board);
    ns.board.set(p.row(), p.column(), symbol);
    return ns;
  }
}
