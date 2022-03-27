package com.github.amshali.rl.fifteen;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;
import me.tongfei.progressbar.ProgressBar;
import sutton.barto.rlbook.Utils;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
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
    System.out.println(s);
    var random = new Random();
    int t = 0;
    while (t < 30 && !s.isTerminal()) {
      t++;
      System.out.println("-----------------");
      Thread.sleep(1000);
      var possibleActions = s.possibleActions();
      System.out.println(policy.containsKey(s.hash()) ? "Has state" : "Does not have state");
      s = s.nextState(policy.getOrDefault(s.hash(),
          possibleActions.get(random.nextInt(possibleActions.size()))));
      System.out.println(s);
    }
  }

  private ToDoubleFunction<Integer> actionValue(State s) {
    return (Integer action) -> {
      var sPrime = s.nextState(action);
      states.putIfAbsent(sPrime.hash(), sPrime);
      sPrime = states.get(sPrime.hash());
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
      states.keySet().parallelStream().forEach(h -> {
        var s = states.get(h);
        if (s.isTerminal()) {
          pb.step();
          return;
        }
        double maxValue =
            s.possibleActions().stream().mapToDouble(this.actionValue(s)).max().orElse(0.0);
        var oldValue = s.value();
        s.setValue(maxValue);
        var localDelta = Math.abs(maxValue - oldValue);
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
    states.keySet().parallelStream().forEach(h -> {
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
      return 1.0;
    }
    return -1.0;
  }

  private void initValues() {
    var pb = new ProgressBar("Generating random states", fInitRandomState);
    states.put(State.TERMINAL.hash(), State.TERMINAL);
    var workQueue = new LinkedBlockingQueue<String>();
    workQueue.offer(State.TERMINAL.hash());
    IntStream.range(0, 2).parallel().forEach((i) -> {
      while (states.size() < fInitRandomState) {
        State current = null;
        try {
          current = states.get(workQueue.take());
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        var currentActions = current.possibleActions();
        State finalCurrent = current;
        currentActions.forEach(a -> {
          var next = finalCurrent.nextState(a);
          if (!states.containsKey(next.hash())) {
            states.put(next.hash(), next);
            workQueue.offer(next.hash());
            pb.step();
          }
        });
      }
    });
    pb.stepTo(fInitRandomState);
    pb.close();
  }
}
