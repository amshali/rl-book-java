package com.github.amshali.rl.fifteen;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;
import me.tongfei.progressbar.ProgressBar;
import sutton.barto.rlbook.Utils;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

public class Fifteen {

  private final Map<String, State> states = new ConcurrentHashMap<>(1_000_000);
  private final Map<String, Integer> policy = new ConcurrentHashMap<>(1_000_000);
  @Parameter(names = {"--init-random", "-ir"}, validateWith = PositiveInteger.class,
      description = "Number of initial random state")
  Integer fInitRandomState = 1_000_000;
  @Parameter(names = {"--gamma", "-g", "--discount-rate"}, description = "Discount rate.")
  Double fDiscountRate = 0.9;
  @Parameter(names = {"--theta", "-th", "--threshold"})
  Double fTheta = 0.1;

  private Random random = new Random();

  public static void main(String[] args) throws InterruptedException {
    var fifteen = new Fifteen();
    JCommander.newBuilder()
        .addObject(fifteen)
        .build()
        .parse(args);
    fifteen.run();
  }

  private void run() throws InterruptedException {
    initValues();
    calculateOptimalValues();
    calculateOptimalPolicy();
    System.out.println("Done!");
    State s = State.randomState();
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
        s = State.randomState();
        continue;
      }
      t++;
      Thread.sleep(1000);
      s = nextState(s, policy.get(s.hash()));
    }
  }

  private State nextState(State s, int action) {
    if (!s.possibleActions().contains(action)) {
      throw new RuntimeException("Invalid action: " + action + " in state: " + s);
    }
    int[] ints = s.cloneNumbers();
    var actionCell = ints[action];
    ints[action] = State.NIL_VALUE;
    ints[s.nilIndex()] = actionCell;
    var next = new State(ints);
    states.putIfAbsent(next.hash(), next);
    return states.get(next.hash());
  }

  private ToDoubleFunction<Integer> actionValue(State s) {
    return (Integer action) -> {
      var sPrime = nextState(s, action);
      return reward(sPrime) + fDiscountRate * sPrime.value();
    };
  }

  public void calculateOptimalValues() {
    var epoch = 0;
    while (true) {
      final var delta = new AtomicReference<>(0.0);
      epoch++;
      System.out.printf("Epoch %d\n", epoch);
      var pb = new ProgressBar("Optimal values", states.keySet().size());
      states.keySet().forEach(h -> {
        var s = states.get(h);
        if (s.isTerminal()) {
          pb.step();
          return;
        }
        double maxValue =
            s.possibleActions().stream().mapToDouble(this.actionValue(s)).max().orElse(0.0);
        var oldValue = s.value();
        s.setValue(maxValue);
        var localDelta = Math.abs(oldValue - maxValue);
        synchronized (delta) {
          delta.set(Math.max(delta.get(), localDelta));
        }
        pb.step();
      });
      pb.close();
      System.out.printf("Δ = %f\n", delta.get());
      if (delta.get() < fTheta) {
        break;
      }
    }
  }

  public void calculateOptimalPolicy() {
    var pb = new ProgressBar("Optimal π", states.keySet().size());
    states.keySet().forEach(h -> {
      var s = states.get(h);
      if (s.isTerminal()) {
        pb.step();
        return;
      }
      var possibleActions = s.possibleActions();
      var actionValues = possibleActions.stream().mapToDouble(this.actionValue(s)).toArray();
      policy.put(s.hash(), possibleActions.get(Utils.argmax(actionValues)));
      pb.step();
    });
    pb.close();
  }

  public Double reward(State s) {
    if (s.isTerminal()) {
      return 10.0;
    }
    return -0.1;
  }

  private void initValues() {
    states.put(State.TERMINAL.hash(), State.TERMINAL);
    var ints = IntStream.range(1, State.NUM_CELLS + 1).boxed().toArray(Integer[]::new);
    var perms = Utils.getPermutationsRecursive(ints);
    perms.forEach(p -> {
      var s = new State(Arrays.stream(p).mapToInt(Integer::intValue).toArray());
      states.putIfAbsent(s.hash(), s);
    });
  }
}
