package sutton.barto.rlbook.chapter05;

import com.beust.jcommander.JCommander;
import me.tongfei.progressbar.ProgressBar;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.HeatMapChart;
import org.knowm.xchart.HeatMapChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.Styler;
import sutton.barto.rlbook.MultiDimArray;
import sutton.barto.rlbook.Utils;

import java.io.IOException;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;

@FunctionalInterface
interface TriFunction<A, B, C, R> {
  R apply(A a, B b, C c);
}

class TrajectoryItem {
  Integer action;
  Map<String, Object> state;

  public TrajectoryItem(Integer action, Map<String, Object> state) {
    this.action = action;
    this.state = state;
  }
}

public class BlackJack {
  public static final String TRAJECTORY_STR = "trajectory";
  public static final String REWARD_STR = "reward";
  public static final String STATE_STR = "state";
  public static int ACTION_HIT = 0;
  public static int ACTION_STAND = 1;
  public static Integer[] ACTIONS = new Integer[]{ACTION_HIT, ACTION_STAND};
  public static String USABLE_ACE_STATE = "usable_ace";
  public static String PLAYER_SUM_STATE = "player_sum";
  public static String DEALER_CARD_STATE = "dealer_card";

  Map<Integer, Integer> policyPlayer = new HashMap<>();
  Map<Integer, Integer> policyDealer = new HashMap<>();
  Random random = new Random();

  public BlackJack() {
    IntStream.range(0, 20).forEach(i -> policyPlayer.put(i, ACTION_HIT));
    IntStream.range(20, 22).forEach(i -> policyPlayer.put(i, ACTION_STAND));
    IntStream.range(0, 17).forEach(i -> policyDealer.put(i, ACTION_HIT));
    IntStream.range(17, 22).forEach(i -> policyDealer.put(i, ACTION_STAND));
  }

  public static void main(String[] args) {
    var blackJack = new BlackJack();
    JCommander.newBuilder()
        .addObject(blackJack)
        .build()
        .parse(args);
    try {
      blackJack.run();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  Map<String, Object> stateOf(Boolean usableAce, Integer playerSum,
                              Integer dealerCard) {
    return Map.of(USABLE_ACE_STATE, usableAce, PLAYER_SUM_STATE, playerSum,
        DEALER_CARD_STATE, dealerCard);
  }

  Integer getCard() {
    return Math.min(random.nextInt(14) + 1, 10);
  }

  public void run() throws IOException {
    figure5_2();
  }

  Integer cardValue(Integer cardId) {
    return cardId == 1 ? 11 : cardId;
  }

  Map<String, Object> playHit(Boolean usableAcePlayer, Integer playerSum) {
    // if hit, get new card
    var card = getCard();
    // Keep track of the ace count. the usable_ace_player flag is insufficient alone as it cannot
    // distinguish between having one ace or two.
    var aceCount = usableAcePlayer ? 1 : 0;
    if (card == 1) {
      aceCount++;
    }
    playerSum += cardValue(card);
    // If the player has a usable ace, use it as 1 to avoid busting and continue.
    while (playerSum > 21 && aceCount > 0) {
      playerSum -= 10;
      aceCount--;
    }
    usableAcePlayer = aceCount == 1;
    return Map.of(PLAYER_SUM_STATE, playerSum, USABLE_ACE_STATE, usableAcePlayer);
  }

  Map<String, Object> play(TriFunction<Boolean, Integer, Integer, Integer> policyPlayer,
                           Map<String, Object> initialState,
                           Integer initialAction) {
    var playerSum = 0;
    // trajectory of player
    var playerTrajectory = new ArrayList<TrajectoryItem>();
    // whether player uses Ace as 11
    var usableAcePlayer = false;
    // dealer status
    var dealerCard1 = 0;
    var dealerCard2 = 0;
    var usableAceDealer = false;

    if (initialState == null) {
      //generate a random initial state
      while (playerSum < 12) {
        // if sum of player is less than 12, always hit
        var card = getCard();
        playerSum += card;
        // If the player's sum is larger than 21, he may hold one or two aces.
        if (playerSum > 21) {
          assert playerSum == 22;
          // last card must be ace
          playerSum -= 10;
        } else {
          usableAcePlayer |= (1 == card);
        }
      }
      // initialize cards of dealer, suppose dealer will show the first card he gets
      dealerCard1 = getCard();
    } else {
      // use specified initial state
      usableAcePlayer = (boolean) initialState.get(USABLE_ACE_STATE);
      playerSum = (int) initialState.get(PLAYER_SUM_STATE);
      dealerCard1 = (int) initialState.get(DEALER_CARD_STATE);
    }
    dealerCard2 = getCard();
    // initial state of the game
    var state = Map.of(USABLE_ACE_STATE, usableAcePlayer, PLAYER_SUM_STATE, playerSum,
        DEALER_CARD_STATE, dealerCard1);
    // initialize dealer's sum
    var dealerSum = cardValue(dealerCard1) + cardValue(dealerCard2);
    usableAceDealer = dealerCard1 == 1 || dealerCard2 == 1;
    // if the dealer's sum is larger than 21, he must hold two aces.
    if (dealerSum > 21) {
      assert dealerSum == 22;
      // use one Ace as 1 rather than 11
      dealerSum -= 10;
    }
    // Game starts:

    // Player's turn
    while (!Thread.interrupted()) {
      Integer action;
      if (initialAction != null) {
        action = initialAction;
        initialAction = null;
      } else {
        // get action based on current sum
        action = policyPlayer.apply(usableAcePlayer, playerSum, dealerCard1);
      }
      // track player's trajectory for importance sampling
      playerTrajectory.add(
          new TrajectoryItem(action, stateOf(usableAcePlayer, playerSum, dealerCard1)));
      if (action == ACTION_STAND) {
        break;
      }
      Map<String, Object> r1 = playHit(usableAcePlayer, playerSum);
      usableAcePlayer = (boolean) r1.get(USABLE_ACE_STATE);
      playerSum = (int) r1.get(PLAYER_SUM_STATE);
      if (playerSum > 21) {
        // player busts
        return Map.of(STATE_STR, state, REWARD_STR, -1, TRAJECTORY_STR, playerTrajectory);
      }
    }

    // Dealer's turn
    while (!Thread.interrupted()) {
      var action = policyDealer.get(dealerSum);
      if (action == ACTION_STAND) {
        break;
      }
      Map<String, Object> r1 = playHit(usableAceDealer, dealerSum);
      usableAceDealer = (boolean) r1.get(USABLE_ACE_STATE);
      dealerSum = (int) r1.get(PLAYER_SUM_STATE);
      if (dealerSum > 21) {
        // dealer loses
        return Map.of(STATE_STR, state, REWARD_STR, 1, TRAJECTORY_STR, playerTrajectory);
      }
    }
    // compare the sum between player and dealer
    if (playerSum > dealerSum) {
      return Map.of(STATE_STR, state, REWARD_STR, 1, TRAJECTORY_STR, playerTrajectory);
    } else if (playerSum == dealerSum) {
      return Map.of(STATE_STR, state, REWARD_STR, 0, TRAJECTORY_STR, playerTrajectory);
    }
    return Map.of(STATE_STR, state, REWARD_STR, -1, TRAJECTORY_STR, playerTrajectory);
  }

  MultiDimArray monteCarloExploringStarts(Integer episodes) {
    // (playerSum, dealerCard, usableAce, action)
    var stateActionValues = new MultiDimArray(0, 10, 10, 2, 2);
    // Initialize counts to 1 to avoid division by 0
    // For computing average of Returns(s, a). It is essentially a counter.
    var stateActionPairCount = new MultiDimArray(1, 10, 10, 2, 2);
    var pb = new ProgressBar("Exploring starts", episodes);
    var behaviorPolicy = new ESBehaviorPolicy(stateActionValues, stateActionPairCount);
    var targetPolicyPlayer = new TargetPolicyPlayer();
    IntStream.range(0, episodes).forEach(episode -> {
      // for each episode, use a randomly initialized state and action
      var initState = stateOf(random.nextBoolean(), random.nextInt(12, 22), random.nextInt(1, 11));
      var initAction = ACTIONS[random.nextInt(ACTIONS.length)];
      var currentPolicy = episode > 0 ? behaviorPolicy : targetPolicyPlayer;
      Map<String, Object> result = play(currentPolicy, initState, initAction);
      var trajectory = (List<TrajectoryItem>) result.get(TRAJECTORY_STR);
      var firstVisitCheck = new HashSet<>();
      trajectory.forEach(t -> {
        var usableAceInt = (Boolean) t.state.get(USABLE_ACE_STATE) ? 1 : 0;
        var playerSum = (Integer) t.state.get(PLAYER_SUM_STATE) - 12;
        var dealerCard = (Integer) t.state.get(DEALER_CARD_STATE) - 1;
        var stateAction = "%d,%d,%d,%d".formatted(usableAceInt, playerSum, dealerCard, t.action);
        if (!firstVisitCheck.contains(stateAction)) {
          firstVisitCheck.add(stateAction);
        } else {
          return;
        }
        // This is essentially appending the return to the list of Returns(s, a)
        stateActionValues.set((d -> d.intValue() + (Integer) result.get(REWARD_STR)), dealerCard,
            playerSum,
            usableAceInt, t.action);
        stateActionPairCount.set((d -> d.intValue() + 1), dealerCard, playerSum, usableAceInt,
            t.action);
      });
      pb.step();
    });
    pb.close();
    return stateActionValues.op(
        (BinaryOperator<Number>) (d1, d2) -> d1.doubleValue() / d2.doubleValue(),
        stateActionPairCount);
  }

  void figure5_2() throws IOException {
    var esStateActionValues = monteCarloExploringStarts(5_000_000);
    var stateValueNoUsableAce = new ArrayList<Number[]>();
    var stateValueUsableAce = new ArrayList<Number[]>();
    var actionNoUsableAce = new ArrayList<Number[]>();
    var actionUsableAce = new ArrayList<Number[]>();
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        var m0 = Double.NEGATIVE_INFINITY;
        var i0 = 0;
        var m1 = Double.NEGATIVE_INFINITY;
        var i1 = 0;
        for (int a = 0; a < 2; a++) {
          var v0 = esStateActionValues.get(i, j, 0, a);
          if (v0.doubleValue() > m0) {
            i0 = a;
            m0 = v0.doubleValue();
          }
          var v1 = esStateActionValues.get(i, j, 1, a);
          if (v1.doubleValue() > m1) {
            i1 = a;
            m1 = v1.doubleValue();
          }
        }
        stateValueNoUsableAce.add(new Number[]{i, j, Utils.round(m0, 2)});
        actionNoUsableAce.add(new Number[]{i, j, i0});
        stateValueUsableAce.add(new Number[]{i, j, Utils.round(m1, 2)});
        actionUsableAce.add(new Number[]{i, j, i1});
      }
    }
    var dealerShowing = new ArrayList<Integer>();
    var playSum = new ArrayList<Integer>();
    for (int i = 0; i < 10; i++) {
      dealerShowing.add(i + 1);
      playSum.add(i + 12);
    }
    var chartValueUsableAce =
        createHeatMap(700, 500, "Optimal value with usable Ace", "dealer showing", "player sum");
    chartValueUsableAce.addSeries("Optimal value with usable Ace", dealerShowing, playSum,
        stateValueUsableAce);
    var chartActionValueUsableAce =
        createHeatMap(700, 500, "Optimal policy with usable Ace", "dealer showing", "player sum");
    chartActionValueUsableAce.addSeries("Optimal policy with usable Ace", dealerShowing, playSum,
        actionUsableAce);
    var chartValueNoUsableAce =
        createHeatMap(700, 500, "Optimal value with NO usable Ace", "dealer showing", "player sum");
    chartValueNoUsableAce.addSeries("Optimal value with NO usable Ace", dealerShowing, playSum,
        stateValueNoUsableAce);
    var chartActionValueNoUsableAce =
        createHeatMap(700, 500, "Optimal policy with NO usable Ace", "dealer showing",
            "player sum");
    chartActionValueNoUsableAce.addSeries("Optimal policy with NO usable Ace", dealerShowing,
        playSum,
        actionNoUsableAce);
    var charts = new ArrayList<HeatMapChart>();
    charts.add(chartActionValueUsableAce);
    charts.add(chartValueUsableAce);
    charts.add(chartActionValueNoUsableAce);
    charts.add(chartValueNoUsableAce);
    BitmapEncoder.saveBitmap(charts, 2, 2, "./images/chapter05-blackjack-es.png",
        BitmapEncoder.BitmapFormat.PNG);
    new SwingWrapper<>(charts, 2, 2).displayChartMatrix();
  }

  private HeatMapChart createHeatMap(int width, int height, String title, String xTitle,
                                     String yTitle) {
    final var chart = new HeatMapChartBuilder().width(width).title(title).height(height)
        .xAxisTitle(xTitle).yAxisTitle(yTitle).theme(Styler.ChartTheme.Matlab).build();
    var styler = chart.getStyler();
    styler.setLegendPosition(Styler.LegendPosition.OutsideE);
    styler.setChartTitleVisible(true);
    return chart;
  }

  /**
   * ES default policy which uses the averages of values for each action so far to find the best
   * one(greedy).
   */
  static class ESBehaviorPolicy implements TriFunction<Boolean, Integer, Integer, Integer> {
    MultiDimArray stateActionValues;
    MultiDimArray stateActionPairCount;

    public ESBehaviorPolicy(MultiDimArray stateActionValues,
                            MultiDimArray stateActionPairCount) {
      this.stateActionValues = stateActionValues;
      this.stateActionPairCount = stateActionPairCount;
    }

    @Override
    public Integer apply(Boolean usableAce, Integer playerSum, Integer dealerCard) {
      var usableAceInt = usableAce ? 1 : 0;
      playerSum -= 12;
      dealerCard -= 1;
      var vs = new Vector<Double>();
      for (int i = 0; i < ACTIONS.length; i++) {
        vs.add(stateActionValues.get(dealerCard, playerSum, usableAceInt, i).doubleValue() /
            stateActionPairCount.get(dealerCard, playerSum, usableAceInt, i).doubleValue());
      }
      return Utils.argmax(vs.toArray(Double[]::new));
    }
  }

  class TargetPolicyPlayer implements TriFunction<Boolean, Integer, Integer, Integer> {
    @Override
    public Integer apply(Boolean usableAce, Integer playerSum, Integer dealerCard) {
      return policyPlayer.get(playerSum);
    }
  }

  class BehaviorPolicyPlayer implements TriFunction<Boolean, Integer, Integer, Integer> {
    @Override
    public Integer apply(Boolean usableAce, Integer playerSum, Integer dealerCard) {
      return random.nextDouble() < 0.5 ? ACTION_STAND : ACTION_HIT;
    }
  }
}
