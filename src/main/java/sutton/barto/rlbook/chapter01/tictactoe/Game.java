package sutton.barto.rlbook.chapter01.tictactoe;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;
import sutton.barto.rlbook.ConsoleColors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Game {

  public static final int BOARD_ROWS = 3;
  public static final int BOARD_COLS = 3;
  public static final int P1_SYMBOL = 1;
  public static final int P2_SYMBOL = -1;
  private final Map<Long, State> allStates;
  private final Random random = new Random();
  @Parameter(names = {"--model-location", "-m"},
      description = "Directory of the model to store estimates.")
  String modelLocationFlag;
  @Parameter(names = {"--train", "-t"}, validateWith = PositiveInteger.class,
      description = "Train the players and write the new models. Parameter value is the number of" +
          " epochs to train."
  )
  Integer trainFlag;
  @Parameter(names = {"--compete", "-c"}, validateWith = PositiveInteger.class,
      description = "Compete the players using the model. Parameter value is the number of turns.")
  Integer competeFlag;
  @Parameter(names = {"--play", "-p"},
      description = "Play against the computer using the trained model.")
  Boolean playFlag = false;
  @Parameter(names = {"--explore-rate", "-er"},
      description = "Exploration rate.")
  Double exploreRate = 0.1;

  public Game() {
    allStates = getAllStates();
  }

  /**
   * Generates all the possible states.
   *
   * @return map of hash to state
   */
  public static Map<Long, State> getAllStates() {
    State currentState = State.init();
    Map<Long, State> all1 = new HashMap<>();
    Map<Long, State> all2 = new HashMap<>();
    all1.put(currentState.hash(), currentState);
    getAllStatesRec(currentState, P1_SYMBOL, all1);
    getAllStatesRec(currentState, P2_SYMBOL, all2);
    all1.putAll(all2);
    return all1;
  }

  private static void getAllStatesRec(State currentState, int currentSymbol,
                                      Map<Long, State> allStates) {
    for (int i = 0; i < BOARD_ROWS; i++) {
      for (int j = 0; j < BOARD_COLS; j++) {
        Position p = new Position(i, j);
        if (currentState.data(p) == 0) {
          State newState = currentState.nextState(p, currentSymbol);
          if (!allStates.containsKey(newState.hash())) {
            allStates.put(newState.hash(), newState);
            if (!newState.end()) {
              getAllStatesRec(newState, -currentSymbol, allStates);
            }
          }
        }
      }
    }
  }

  public static void main(String[] args) {
    Game game = new Game();
    JCommander.newBuilder()
        .addObject(game)
        .build()
        .parse(args);
    game.run();
  }

  public void train(int epochs) throws IOException {
    IPlayer p1 = new Player(P1_SYMBOL, 0.1, exploreRate, allStates);
    IPlayer p2 = new Player(P2_SYMBOL, 0.1, exploreRate, allStates);
    Judge judge = new Judge(p1, p2, true);
    double player1Wins = 0;
    double player2Wins = 0;
    double ties = 0;
    for (int i = 0; i < epochs; i++) {
      System.out.printf("Epoch %d\n", i);
      int winner = judge.play(false);
      if (winner == Game.P1_SYMBOL) {
        player1Wins++;
      } else if (winner == Game.P2_SYMBOL) {
        player2Wins++;
      } else {
        ties++;
      }
      judge.reset();
    }
    System.out.printf("Player 1 wins: %.2f\n", player1Wins / epochs);
    System.out.printf("Player 2 wins: %.2f\n", player2Wins / epochs);
    System.out.printf("Ties: %.2f\n", ties / epochs);
    p1.savePolicy(new File(Path.of(modelLocationFlag, "./p1_estimates.obj").toUri()));
    p2.savePolicy(new File(Path.of(modelLocationFlag, "./p2_estimates.obj").toUri()));
  }

  public void compete(int turns) throws IOException, ClassNotFoundException {
    IPlayer p1 = new Player(P1_SYMBOL, 0.0, 0.0, allStates);
    IPlayer p2 = new Player(P2_SYMBOL, 0.0, 0.0, allStates);
    Judge judge = new Judge(p1, p2, false);
    p1.loadPolicy(new File(Path.of(modelLocationFlag, "./p1_estimates.obj").toUri()));
    p2.loadPolicy(new File(Path.of(modelLocationFlag, "./p2_estimates.obj").toUri()));
    double player1Wins = 0;
    double player2Wins = 0;
    double ties = 0;
    for (int i = 0; i < turns; i++) {
      System.out.printf("Turn %d\n", i);
      int winner = judge.play(false);
      if (winner == P1_SYMBOL) {
        player1Wins++;
      } else if (winner == P2_SYMBOL) {
        player2Wins++;
      } else {
        ties++;
      }
      judge.reset();
    }
    System.out.printf("Player 1 wins: %.2f\n", player1Wins / turns);
    System.out.printf("Player 2 wins: %.2f\n", player2Wins / turns);
    System.out.printf("Ties: %.2f\n", ties / turns);
  }

  public void play() throws IOException, ClassNotFoundException {
    while (true) {
      boolean humanFirst = Math.abs(random.nextInt()) % 2 == 0;
      IPlayer player1, player2;
      int humanSymbol;
      int computerSymbol;
      if (humanFirst) {
        player1 = new HumanPlayer(P1_SYMBOL);
        humanSymbol = P1_SYMBOL;
        System.out.println("You are 'O'.");
        computerSymbol = P2_SYMBOL;
        player2 = new Player(P2_SYMBOL, 0, 0, allStates);
        player2.loadPolicy(new File(Path.of(modelLocationFlag, "./p2_estimates.obj").toUri()));
      } else {
        player2 = new HumanPlayer(P2_SYMBOL);
        System.out.println("You are 'X'.");
        humanSymbol = P2_SYMBOL;
        computerSymbol = P1_SYMBOL;
        player1 = new Player(P1_SYMBOL, 0, 0, allStates);
        player1.loadPolicy(new File(Path.of(modelLocationFlag, "./p1_estimates.obj").toUri()));
      }
      Judge judge = new Judge(player1, player2, false);
      int winner = judge.play(true);
      if (winner == humanSymbol) {
        System.out.printf("%sYou won!%s\n", ConsoleColors.GREEN_BRIGHT, ConsoleColors.RESET);
      } else if (winner == computerSymbol) {
        System.out.printf("%sYou lost!%s\n", ConsoleColors.RED_BRIGHT, ConsoleColors.RESET);
      } else {
        System.out.println("Tie!");
      }
      System.out.println("=========================================");
    }
  }

  public void run() {
    if (trainFlag != null) {
      try {
        train(trainFlag);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (competeFlag != null) {
      try {
        compete(competeFlag);
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
    if (playFlag) {
      try {
        play();
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
  }
}
