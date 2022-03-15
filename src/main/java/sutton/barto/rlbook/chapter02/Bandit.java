package sutton.barto.rlbook.chapter02;

import org.apache.commons.math3.distribution.NormalDistribution;
import sutton.barto.rlbook.Utils;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class Bandit {
  private int kArms = 10;
  /**
   * This is for ε-greedy algorithm.
   */
  private double epsilon = 0.0;
  private double initial = 0.0;
  /**
   * Learning parameter for updating estimates.
   */
  private double stepSize = 0.1;
  /**
   * If true, use sample averages to update estimations instead of constant step size.
   */
  private boolean sampleAverages = false;
  private Double ucbParam = null;
  /**
   * If true, use gradient based bandit algorithm.
   */
  private boolean gradient = false;
  /**
   * If true, use average reward as baseline for gradient based bandit algorithm.
   */
  private boolean gradientBaseline = false;
  private double trueReward = 0.0;

  private int[] indices;

  /**
   * Real reward for each action.
   */
  private double[] qTrue;

  /**
   * Estimation for each action.
   */
  private double[] qEst;

  /**
   * Number of chosen times for each action.
   */
  private int[] actionCount;

  private int time = 0;

  private int bestAction = -1;

  private double averageReward = 0.0;

  private final Random random = new Random();

  public Bandit() {
    this(10, 0.0, 0.0, 0.1, false, null, false, false, 0.0);
  }

  public Bandit(int kArms, double epsilon, double initial, double stepSize, boolean sampleAverages,
                Double ucbParam, boolean gradient, boolean gradientBaseline, double trueReward) {
    this.kArms = kArms;
    this.epsilon = epsilon;
    this.initial = initial;
    this.stepSize = stepSize;
    this.sampleAverages = sampleAverages;
    this.ucbParam = ucbParam;
    this.gradient = gradient;
    this.gradientBaseline = gradientBaseline;
    this.trueReward = trueReward;
    indices = IntStream.range(0, kArms).toArray();
    NormalDistribution normalDistribution = new NormalDistribution(0, 1);
    IntStream.range(0, kArms).forEach(i -> {
      qTrue[i] = normalDistribution.sample() + trueReward;
      qEst[i] = initial;
      actionCount[i] = 0;
    });
    bestAction = Utils.argmax(qTrue);
  }

  public int getAction() {
    if (epsilon > 0) {
      // Take a random action with probability of ε.
      if (random.nextDouble() < epsilon) {
        return indices[random.nextInt(indices.length)];
      }
    }
    if (ucbParam != null) {
      // TODO: Implement!
    }
    if (gradient) {
      DoubleStream qEstExp = Arrays.stream(qEst).map(Math::exp);
      double sum = qEstExp.sum();
      double[] actionProb = qEstExp.map(d -> d / sum).toArray();
      double rnd = random.nextDouble();
      for (int i = 0; i < actionProb.length; i++) {
        if (rnd < actionProb[i]) {
          return indices[i];
        }
      }
    }
    return Utils.argmax(qEst);
  }

  /**
   * Takes an action, adjust the estimates and returns the received reward.
   * @param action the arm number
   * @return the reward
   */
  public double takeAction(int action) {
    NormalDistribution normalDistribution = new NormalDistribution(0, 1);
    double reward = normalDistribution.sample() + qTrue[action];
    time++;
    averageReward = (time - 1.0) / (time * averageReward) + reward / time;
    actionCount[action]++;
    if (sampleAverages) {
      // Exponential-recency weighted average:
      qEst[action] += (reward - qEst[action]) / actionCount[action];
    } else if (gradient) {

    } else {
      // Update estimation with constant step size
      qEst[action] += stepSize * (reward - qEst[action]);
    }
    return reward;
  }

}
