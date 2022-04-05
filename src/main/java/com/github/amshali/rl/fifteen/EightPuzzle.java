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

public class EightPuzzle {

  private final Map<String, EightState> states = new ConcurrentHashMap<>(1_000_000);
  private final Map<String, Integer> policy = new ConcurrentHashMap<>(1_000_000);
  private final Random random = new Random();
  @Parameter(names = {"--gamma", "-g", "--discount-rate"}, description = "Discount rate.")
  Double fDiscountRate = 0.9;
  @Parameter(names = {"--theta", "-th", "--threshold"})
  Double fTheta = 0.1;

  public static void main(String[] args) throws InterruptedException {
    var fifteen = new EightPuzzle();
    JCommander.newBuilder()
        .addObject(fifteen)
        .build()
        .parse(args);
    fifteen.run();
  }

  private void run() throws InterruptedException {
    init();
    calculateOptimalValues();
    calculateOptimalPolicy();
    System.out.println("Done!");
    var allWorse = new ArrayList<EightState>();
    states.keySet().parallelStream().forEach(h -> {
      var s = states.get(h);
      if (s.isTerminal()) {
        return;
      }
      int worse = 0;
      for (var a : s.actionState.keySet()) {
        var next = states.get(s.actionState.get(a));
        if (s.value() > next.value() && !next.isTerminal()) {
          worse++;
        }
      }
      if (worse == s.actionState.size()) {
        allWorse.add(s);
        System.out.println(s);
        System.out.println("^^^^^^^^^^^^^");
      }
    });
    System.out.println("#of states with no better next state = " + allWorse.size());
    var allStates = states.values().toArray(EightState[]::new);
    EightState s = allStates[random.nextInt(allStates.length)];
    int t = 0;
    final Scanner input = new Scanner(System.in);
    while (true) {
      System.out.printf("T = %d\n", t);
      System.out.println("--------------");
      System.out.println(s);
      if (s.isTerminal()) {
        System.out.println("===========================================");
        System.out.println("Again? (y/n)");
        var in = input.next();
        if (!in.equalsIgnoreCase("y")) {
          break;
        }
        t = 0;
        s = allStates[random.nextInt(allStates.length)];
        continue;
      }
      t++;
      Thread.sleep(1000);
      s = nextState(s, policy.get(s.hash()));
    }
  }

  private EightState nextState(EightState s, int action) {
    var next = EightState.nextState(s, action);
    return states.get(next.hash());
  }

  private ToDoubleFunction<Integer> actionValue(EightState s) {
    return (Integer action) -> {
      var nextState = nextState(s, action);
      return reward(nextState) + fDiscountRate * nextState.value();
    };
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
      var possibleActions = s.possibleActions();
      var actionValues = possibleActions.stream().mapToDouble(this.actionValue(s)).toArray();
      var bestAction = possibleActions.get(Utils.argmax(actionValues));
      policy.put(h, bestAction);
      pb.step();
    });
    pb.close();
  }

  public Double reward(EightState ns) {
    if (ns.isTerminal()) {
      return 10.0;
    }
    return -1.0;
  }

  private void init() {
    var work = new LinkedBlockingQueue<EightState>();
    work.offer(EightState.TERMINAL);
    states.putIfAbsent(EightState.TERMINAL.hash(), EightState.TERMINAL);
    var seen = new HashSet<>();
    seen.add(EightState.TERMINAL.hash());
    while (work.size() > 0) {
      var w = work.poll();
      w.possibleActions().forEach(a -> {
        var ns = EightState.nextState(w, a);
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
        var ns = nextState(s, a);
        s.actionState.put(a, ns.hash());
      });
    });
  }
}
