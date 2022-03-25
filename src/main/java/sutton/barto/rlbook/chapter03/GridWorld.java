package sutton.barto.rlbook.chapter03;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import j2html.tags.DomContent;
import org.apache.commons.math3.linear.*;
import sutton.barto.rlbook.Matrix;
import sutton.barto.rlbook.Position;
import sutton.barto.rlbook.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;

import static j2html.TagCreator.*;

public class GridWorld {
  public static final int WORLD_SIZE = 5;
  public static final Double ACTION_PROB = 0.25;
  public static final Position A_POS = new Position(0, 1);
  public static final Position A_PRIME_POS = new Position(4, 1);
  public static final Position B_POS = new Position(0, 3);
  public static final Position B_PRIME_POS = new Position(2, 3);
  public static final char[] ACTIONS_FIGS = new char[]{'←', '↑', '→', '↓'};
  public static final Vector<int[]> ACTIONS = Utils.vectorOf(
      new int[]{0, -1}/* left */, new int[]{-1, 0} /* up */,
      new int[]{0, 1}/* right */, new int[]{1, 0} /* down */);
  private final RealMatrixPreservingVisitor sumAbsOp = new DefaultRealMatrixPreservingVisitor() {
    private Double sum = 0.0;

    @Override
    public void start(int rows, int columns, int startRow, int endRow, int startColumn,
                      int endColumn) {
      sum = 0.0;
    }

    @Override
    public void visit(int row, int column, double value) {
      sum += Math.abs(value);
    }

    @Override
    public double end() {
      return sum;
    }
  };
  @Parameter(names = {"--gamma", "-g", "--discount-rate"}, description = "Discount rate.")
  Double discountRate = 0.9;
  @Parameter(names = {"--output",
      "-o"}, description = "The output html file path.", required = true)
  String outputFile;

  public static void main(String[] args) {
    var game = new GridWorld();
    JCommander.newBuilder()
        .addObject(game)
        .build()
        .parse(args);
    game.run();
  }

  private Position getNextState(Position state, int[] action) {
    return new Position(state.row() + action[0], state.column() + action[1]);
  }

  public Map<String, Object> step(Position state, int[] action) {
    if (state.equals(A_POS)) {
      return Map.of("next_state", A_PRIME_POS, "reward", 10.0);
    }
    if (state.equals(B_POS)) {
      return Map.of("next_state", B_PRIME_POS, "reward", 5.0);
    }
    var nextState = getNextState(state, action);
    var reward = 0.0;
    if (nextState.row() < 0 || nextState.row() >= WORLD_SIZE || nextState.column() < 0 ||
        nextState.column() >= WORLD_SIZE) {
      reward = -1.0;
      nextState = state;
    } else {
      reward = 0;
    }
    return Map.of("next_state", nextState, "reward", reward);
  }

  private DomContent figure3_2() throws IOException {
    var value = Utils.zeros(WORLD_SIZE, WORLD_SIZE);
    while (!Thread.interrupted()) {
      var newValue = Utils.zeros(WORLD_SIZE, WORLD_SIZE);
      for (int i = 0; i < WORLD_SIZE; i++) {
        for (int j = 0; j < WORLD_SIZE; j++) {
          for (var action : ACTIONS) {
            var result = step(new Position(i, j), action);
            var nextState = (Position) result.get("next_state");
            var reward = (Double) result.get("reward");
            newValue.setEntry(i, j, newValue.getEntry(i, j) +
                ACTION_PROB * (reward + discountRate * value.getEntry(nextState.row(),
                    nextState.column())));
          }
        }
      }
      var diff = value.subtract(newValue);
      var sumAbsValues = diff.walkInRowOrder(sumAbsOp);
      if (sumAbsValues < 1e-4) {
        return GridWorldDraw.drawMatrix(newValue, "Figure 3.2 - State-Value function");
      }
      value = MatrixUtils.createRealMatrix(newValue.getData());
    }
    return null;
  }

  private DomContent figure3_2LinearAlgebra() throws IOException {
    RealMatrix result = computeStateValueUsingLinearAlgebra();
    return GridWorldDraw.drawMatrix(result, "Figure 3.2 - State-Value function using linear " +
        "system");
  }

  private RealMatrix computeStateValueUsingLinearAlgebra() {
    var coefficients = MatrixUtils.createRealMatrix(WORLD_SIZE * WORLD_SIZE,
        WORLD_SIZE * WORLD_SIZE);
    var constants = new ArrayRealVector(WORLD_SIZE * WORLD_SIZE);
    for (int i = 0; i < WORLD_SIZE * WORLD_SIZE; i++) {
      coefficients.setEntry(i, i, -1.0);
      constants.setEntry(i, 0.0);
    }
    for (int i = 0; i < WORLD_SIZE; i++) {
      for (int j = 0; j < WORLD_SIZE; j++) {
        var state = new Position(i, j);
        var stateIndex = i * WORLD_SIZE + j;
        for (var action : ACTIONS) {
          Map<String, Object> result = step(state, action);
          var nextState = (Position) result.get("next_state");
          var reward = (Double) result.get("reward");
          var nextStateIndex = nextState.row() * WORLD_SIZE + nextState.column();
          coefficients.setEntry(stateIndex, nextStateIndex,
              coefficients.getEntry(stateIndex, nextStateIndex) + ACTION_PROB * discountRate);
          constants.setEntry(stateIndex, constants.getEntry(stateIndex) - ACTION_PROB * reward);
        }
      }
    }
    DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();
    RealVector solution = solver.solve(constants);
    var out = MatrixUtils.createRealMatrix(WORLD_SIZE, WORLD_SIZE);
    for (int i = 0; i < WORLD_SIZE; i++) {
      for (int j = 0; j < WORLD_SIZE; j++) {
        out.setEntry(i, j, solution.getEntry(i * WORLD_SIZE + j));
      }
    }
    return out;
  }

  private Map<String, Object> computeOptimalPolicyAndValue() {
    var value = Utils.zeros(WORLD_SIZE, WORLD_SIZE);
    var policy = new Matrix<Vector<Character>>(WORLD_SIZE, WORLD_SIZE, new Vector<>());
    while (!Thread.interrupted()) {
      var newValue = Utils.zeros(WORLD_SIZE, WORLD_SIZE);
      for (int i = 0; i < WORLD_SIZE; i++) {
        for (int j = 0; j < WORLD_SIZE; j++) {
          var bestActionValue = Double.NEGATIVE_INFINITY;
          var bestActions = new Vector<Character>();
          int k = 0;
          for (var action : ACTIONS) {
            var result = step(new Position(i, j), action);
            var nextState = (Position) result.get("next_state");
            var reward = (Double) result.get("reward");
            var av = reward + discountRate * value.getEntry(nextState.row(),
                nextState.column());
            if (av > bestActionValue) {
              bestActionValue = av;
              bestActions.clear();
              bestActions.add(ACTIONS_FIGS[k]);
            } else if (av == bestActionValue) {
              bestActions.add(ACTIONS_FIGS[k]);
            }
            k++;
          }
          newValue.setEntry(i, j, bestActionValue);
          policy.set(i, j, bestActions);
        }
      }
      var diff = value.subtract(newValue);
      var sumAbsValues = diff.walkInRowOrder(sumAbsOp);
      if (sumAbsValues < 1e-4) {
        return Map.of("value", newValue, "policy", policy);
      }
      value = MatrixUtils.createRealMatrix(newValue.getData());
    }
    return null;
  }

  private DomContent figure3_5() throws IOException {
    Map<String, Object> result = computeOptimalPolicyAndValue();
    assert result != null;
    var value =
        GridWorldDraw.drawMatrix((RealMatrix) result.get("value"), "Figure 3.5 - Optimal Values");
    var policy =
        GridWorldDraw.drawPolicy((Matrix<Vector<Character>>) result.get("policy"), "Figure 3.5" +
            " - Optimal Policy");
    return table(tr(td(value), td(policy)));
  }

  public void run() {
    try {
      computeStateValueUsingLinearAlgebra();
      var f1 = figure3_2();
      var f2 = figure3_2LinearAlgebra();
      var f3 = figure3_5();
      GridWorldDraw.generateHtml(new File(outputFile), table(tr(td(f1), td(f2))), div(f3));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
