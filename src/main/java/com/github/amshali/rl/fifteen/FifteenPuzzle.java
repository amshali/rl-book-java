package com.github.amshali.rl.fifteen;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import me.tongfei.progressbar.ProgressBar;
import sutton.barto.rlbook.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToDoubleFunction;

public class FifteenPuzzle {
  private final Map<String, FifteenState> states =
      new ConcurrentHashMap<>(500_000);
  private final Map<String, Integer> policy = new ConcurrentHashMap<>(500_000);
  private final Random random = new Random();
  @Parameter(names = {"--gamma", "-g", "--discount-rate"}, description = "Discount rate.")
  Double fDiscountRate = 0.9;
  @Parameter(names = {"--theta", "-th", "--threshold"})
  Double fTheta = 0.1;

  public static void main(String[] args) {
    var fifteen = new FifteenPuzzle();
    JCommander.newBuilder()
        .addObject(fifteen)
        .build()
        .parse(args);
    try {
      fifteen.run();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private FifteenState nextState(FifteenState s, int action) {
    var next = s.nextState(action);
    return states.get(next.hash());
  }

  private ToDoubleFunction<Integer> actionValue(FifteenState s) {
    return (Integer action) -> {
      var nextState = nextState(s, action);
      return reward(s, nextState) + fDiscountRate * nextState.value();
    };
  }

  public Double reward(FifteenState currentState, FifteenState nextState) {
    if (nextState.isSolved()) {
      return 40.0;
    }
    if (nextState.rowsSolved() <= 2 && currentState.rowsSolved() <= 2) {
      return 2.0 * (nextState.rowsSolved() - currentState.rowsSolved());
    }
    return -1.0;
  }

  public void run() throws InterruptedException {
    // Generating state space
    init(FifteenState.ONE_TO_FOUR_SOLVED_STATE, FifteenPuzzleEpisode.ONE_TO_FOUR);
    init(FifteenState.FIVE_TO_EIGHT_SOLVED_STATE, FifteenPuzzleEpisode.FIVE_TO_EIGHT);
    init(FifteenState.SOLVED_STATE, FifteenPuzzleEpisode.NINE_TO_FIFTEEN);
    calculateOptimalValues();
    calculateOptimalPolicy();
    printBadStates();
    System.out.println("Done!");

    int totalTime = 0;
    int trials = 10_000;
    var pb = new ProgressBar("Solves", trials);
    for (int i = 0; i < trials; i++) {
      totalTime += solve(generateRandomState());
      pb.step();
    }
    System.out.printf("Average steps per solve = %f\n", (totalTime + 0.0) / trials);
  }

  private int solve(FifteenState currentState) {
    int t = 0;
    while (true) {
      if (currentState.isSolved()) {
        return t;
      }
      t++;
      var action = policy.get(currentState.hash());
      currentState = currentState.nextState(action);
    }
  }

  private FifteenState generateRandomState() {
    var s = FifteenState.SOLVED_STATE;
    var t = 0;
    while (t < 10000) {
      var actions = s.possibleActions();
      s = s.nextState(actions.get(random.nextInt(actions.size())));
      t++;
    }
    return s;
  }

  private void printBadStates() {
    var allWorse = new ArrayList<FifteenState>();
    states.keySet().forEach(h -> {
      var s = states.get(h);
      if (s.isTerminal()) {
        return;
      }
      int worse = 0;
      for (var a : s.actionState().keySet()) {
        var next = states.get(s.actionState().get(a));
        if (s.value() > next.value() && !next.isTerminal()) {
          worse++;
        }
      }
      if (worse == s.actionState().size()) {
        allWorse.add(s);
        System.out.println(s);
        System.out.println("^^^^^^^^^^^^^^^^^");
      }
    });
    System.out.println("#of states with no better next state = " + allWorse.size());
  }

  public void calculateOptimalValues() {
    var epoch = 0;
    while (true) {
      epoch++;
      System.out.printf("Epoch %d\n", epoch);
      final var delta = new AtomicReference<>(0.0);
      var pb = new ProgressBar("Optimal V", states.keySet().size());
      states.keySet().parallelStream().forEach(h -> {
        var s = states.get(h);
        if (s.isTerminal()) {
          pb.step();
          return;
        }
        var oldValue = s.value();
        s.setValue(s.possibleActions().stream().mapToDouble(actionValue(s)).max().orElse(0.0));
        var localDelta = Math.abs(oldValue - s.value());
        synchronized (delta) {
          delta.set(Math.max(delta.get(), localDelta));
        }
        pb.step();
      });
      pb.close();
      System.out.println("Δ = " + delta.get());
      if (delta.get() < fTheta) {
        break;
      }
    }
  }

  public void calculateOptimalPolicy() {
    var pb = new ProgressBar("Optimal π", states.keySet().size());
    states.keySet().parallelStream().forEach(h -> {
      var s = states.get(h);
      if (s.isTerminal()) {
        pb.step();
        return;
      }
      var actions = s.possibleActions();
      var actionValues = actions.stream().mapToDouble(this.actionValue(s)).toArray();
      var bestAction = actions.get(Utils.argmax(actionValues));
      policy.put(h, bestAction);
      pb.step();
    });
    pb.close();
  }

  private void init(FifteenState initState, FifteenPuzzleEpisode episode) {
    var work = new LinkedBlockingQueue<FifteenState>();
    work.offer(initState);
    var seen = new HashSet<>();
    while (work.size() > 0) {
      var w = work.poll();
      states.putIfAbsent(w.hash(), w);
      if (!w.isTerminal()) {
        w.setValue(random.nextDouble());
      }
      w.actionsOf(episode).forEach(a -> {
        var ns = w.nextState(a);
        if (!seen.contains(ns.hash())) {
          states.putIfAbsent(ns.hash(), ns);
          var goodActions = ns.goodActions();
          policy.put(ns.hash(), goodActions.get(random.nextInt(goodActions.size())));
          work.offer(ns);
          seen.add(ns.hash());
        }
      });
    }
    states.keySet().parallelStream().forEach(h -> {
      var s = states.get(h);
      s.goodActions().forEach(a -> {
        var ns = s.nextState(a);
        s.actionState().put(a, ns.hash());
      });
    });
  }
}
