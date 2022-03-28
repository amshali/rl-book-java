package com.github.amshali.rl.fifteen;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import me.tongfei.progressbar.ProgressBar;
import sutton.barto.rlbook.Utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToDoubleFunction;

public class FifteenPuzzle {
  private final Map<FifteenPuzzleEpisode, Map<String, FifteenState>> episodeStates =
      new ConcurrentHashMap<>(1_000_000);
  private final Map<String, Integer> policy = new ConcurrentHashMap<>(1_000_000);
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

  private FifteenState nextState(FifteenState s, int action, FifteenPuzzleEpisode episode) {
    var next = s.nextState(action);
    return episodeStates.get(episode).get(next.hash());
  }

  private ToDoubleFunction<Integer> actionValue(FifteenState s, FifteenPuzzleEpisode episode) {
    return (Integer action) -> {
      var nextState = nextState(s, action, episode);
      return reward(nextState) + fDiscountRate * nextState.value();
    };
  }

  public Double reward(FifteenState ns) {
    if (ns.isTerminal()) {
      return 10.0;
    }
    return -1.0;
  }

  public void run() throws InterruptedException {
    init(FifteenPuzzleEpisode.ONE_TO_FOUR);
    init(FifteenPuzzleEpisode.FIVE_TO_EIGHT);
    init(FifteenPuzzleEpisode.NINE_TO_FIFTEEN);
    calculateOptimalValues(FifteenPuzzleEpisode.ONE_TO_FOUR);
    calculateOptimalPolicy(FifteenPuzzleEpisode.ONE_TO_FOUR);
    printBadStates(FifteenPuzzleEpisode.ONE_TO_FOUR);
    calculateOptimalValues(FifteenPuzzleEpisode.FIVE_TO_EIGHT);
    calculateOptimalPolicy(FifteenPuzzleEpisode.FIVE_TO_EIGHT);
    printBadStates(FifteenPuzzleEpisode.FIVE_TO_EIGHT);
    calculateOptimalValues(FifteenPuzzleEpisode.NINE_TO_FIFTEEN);
    calculateOptimalPolicy(FifteenPuzzleEpisode.NINE_TO_FIFTEEN);
    printBadStates(FifteenPuzzleEpisode.NINE_TO_FIFTEEN);
    System.out.println("Done!");
    int t = 0;
    final Scanner input = new Scanner(System.in);
    var currentState = generateRandomState();
    var currentEpisode = FifteenPuzzleEpisode.ONE_TO_FOUR;
    var stateInEpisode = new FifteenState(currentState.numbers(), currentEpisode);
    while (true) {
      System.out.printf("T = %d\n", t);
      System.out.println("-----------------");
      System.out.println(currentState);
      if (currentState.isTerminal()) {
        System.out.println("===========================================");
        System.out.println("Again? (y/n)");
        var in = input.next();
        if (!in.equalsIgnoreCase("y")) {
          break;
        }
        t = 0;
        currentState = generateRandomState();
        currentEpisode = FifteenPuzzleEpisode.ONE_TO_FOUR;
        stateInEpisode = new FifteenState(currentState.numbers(), currentEpisode);
        continue;
      }
      if (stateInEpisode.isTerminal()) {
        switch (currentEpisode) {
          case ONE_TO_FOUR -> currentEpisode = FifteenPuzzleEpisode.FIVE_TO_EIGHT;
          case FIVE_TO_EIGHT -> currentEpisode = FifteenPuzzleEpisode.NINE_TO_FIFTEEN;
          case NINE_TO_FIFTEEN -> throw new RuntimeException("Should not get here.");
        }
        stateInEpisode = new FifteenState(currentState.numbers(), currentEpisode);
      }
      t++;
      Thread.sleep(1000);
      var action = policy.get(stateInEpisode.hash());
      currentState = currentState.nextState(action);
      stateInEpisode = stateInEpisode.nextState(action);
    }
  }

  private FifteenState generateRandomState() {
    var s = FifteenState.terminalStates.get(FifteenPuzzleEpisode.ONE_TO_FIFTEEN);
    var t = 0;
    while (t < 10000) {
      var actions = s.possibleActions();
      s = s.nextState(actions.get(random.nextInt(actions.size())));
      t++;
    }
    return s;
  }

  private void printBadStates(FifteenPuzzleEpisode episode) {
    var allWorse = new ArrayList<FifteenState>();
    var states = episodeStates.get(episode);
    states.keySet().parallelStream().forEach(h -> {
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
        System.out.println("^^^^^^^^^^^^^");
      }
    });
    System.out.println("#of states with no better next state = " + allWorse.size());
  }

  public void calculateOptimalValues(FifteenPuzzleEpisode episode) {
    var epoch = 0;
    var states = episodeStates.get(episode);
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
        s.setValue(
            s.possibleActions().stream().mapToDouble(actionValue(s, episode)).max().orElse(0.0));
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

  public void calculateOptimalPolicy(FifteenPuzzleEpisode episode) {
    var states = episodeStates.get(episode);
    var pb = new ProgressBar("Optimal π", states.keySet().size());
    states.keySet().parallelStream().forEach(h -> {
      var s = states.get(h);
      if (s.isTerminal()) {
        pb.step();
        return;
      }
      var possibleActions = s.possibleActions();
      var actionValues =
          possibleActions.stream().mapToDouble(this.actionValue(s, episode)).toArray();
      var bestAction = possibleActions.get(Utils.argmax(actionValues));
      policy.put(h, bestAction);
      pb.step();
    });
    pb.close();
  }

  private void init(FifteenPuzzleEpisode episode) {
    episodeStates.putIfAbsent(episode, new ConcurrentHashMap<>());
    var states = episodeStates.get(episode);
    var work = new LinkedBlockingQueue<FifteenState>();
    var t = FifteenState.terminalStates.get(episode);
    work.offer(t);
    states.putIfAbsent(t.hash(), t);
    var seen = new HashSet<>();
    seen.add(t.hash());
    while (work.size() > 0) {
      var w = work.poll();
      w.possibleActions().forEach(a -> {
        var ns = w.nextState(a);
        if (!seen.contains(ns.hash())) {
          states.putIfAbsent(ns.hash(), ns);
          var possibleActions = ns.possibleActions();
          policy.put(ns.hash(), possibleActions.get(random.nextInt(possibleActions.size())));
          work.offer(ns);
          seen.add(ns.hash());
        }
      });
    }
    states.keySet().parallelStream().forEach(h -> {
      var s = states.get(h);
      s.possibleActions().forEach(a -> {
        var ns = s.nextState(a);
        s.actionState().put(a, ns.hash());
      });
    });
  }
}
