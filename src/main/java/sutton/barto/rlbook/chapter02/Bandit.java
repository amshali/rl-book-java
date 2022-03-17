package sutton.barto.rlbook.chapter02;

import org.apache.commons.math3.distribution.NormalDistribution;
import sutton.barto.rlbook.Utils;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class Bandit {
  private final int[] indices;
  private final Random random = new Random();
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
  /**
   * Upper-Confidence-Bound method parameter.
   */
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
    reset();
  }

  public static BanditBuilder builder() {
    return new BanditBuilder();
  }

  public Double ucbParam() {
    return ucbParam;
  }

  public double epsilon() {
    return epsilon;
  }

  public double initial() {
    return initial;
  }

  public int getAction() {
    if (epsilon > 0) {
      // Take a random action with probability of ε.
      if (random.nextDouble() < epsilon) {
        return indices[random.nextInt(indices.length)];
      }
    }
    if (ucbParam != null) {
      // UCB is a little counter-intuitive at first, because it looks like it is giving a higher
      // value to the action that is selected, so why are we uncertain about that? The idea is that
      // we give it a high value so that it is selected next time and so that we can examine the
      // reward it produces until we are certain whether it is good or not.
      var timeLog = Math.log(time + 1);
      var uncertainties =
          Arrays.stream(actionCount).mapToDouble(d -> ucbParam * Math.sqrt(timeLog / (d + 1e-5)))
              .toArray();
      var ucbEstimation =
          IntStream.range(0, qEst.length).mapToDouble(i -> qEst[i] + uncertainties[i]).toArray();
      return Utils.argmax(ucbEstimation);
    }
    if (gradient) {
      var qEstExp = Arrays.stream(qEst).map(Math::exp);
      var sum = qEstExp.sum();
      var actionProb = qEstExp.map(d -> d / sum).toArray();
      var rnd = random.nextDouble();
      for (int i = 0; i < actionProb.length; i++) {
        if (rnd < actionProb[i]) {
          return indices[i];
        }
      }
    }
    // Default to full greedy algorithm:
    return Utils.argmax(qEst);
  }

  /**
   * Takes an action, adjust the estimates and returns the received reward.
   *
   * @param action the arm number
   * @return the reward
   */
  public double takeAction(int action) {
    var normalDistribution = new NormalDistribution(0, 1);
    double reward = normalDistribution.sample() + qTrue[action];
    time++;
    averageReward = (time - 1.0) / (time * averageReward) + reward / time;
    actionCount[action]++;
    if (sampleAverages) {
      // Sample average method for reward estimation. Better for stationary problems.
      qEst[action] += (reward - qEst[action]) / actionCount[action];
    } else if (gradient) {

    } else {
      // Exponential-recency weighted average. Better for non-stationary problems.
      // Update estimation with constant step size:
      qEst[action] += stepSize * (reward - qEst[action]);
    }
    return reward;
  }

  public void reset() {
    var normalDistribution = new NormalDistribution(0, 1);
    qTrue = new double[kArms];
    qEst = new double[kArms];
    actionCount = new int[kArms];
    IntStream.range(0, kArms).forEach(i -> {
      qTrue[i] = normalDistribution.sample() + trueReward;
      // Initial bias value for estimates.
      qEst[i] = initial;
      actionCount[i] = 0;
    });
    bestAction = Utils.argmax(qTrue);
    time = 0;
  }

  public int bestAction() {
    return bestAction;
  }

  public static class BanditBuilder {
    private int kArms = 10;
    private double epsilon = 0.0;
    private double initial = 0.0;
    private double stepSize = 0.1;
    private boolean sampleAverages = false;
    private Double ucbParam = null;
    private boolean gradient = false;
    private boolean gradientBaseline = false;
    private double trueReward = 0.0;

    public BanditBuilder ucbParam(Double ucbParam) {
      this.ucbParam = ucbParam;
      return this;
    }

    public Bandit build() {
      return new Bandit(kArms, epsilon, initial, stepSize, sampleAverages, ucbParam, gradient,
          gradientBaseline, trueReward);
    }

    public BanditBuilder kArms(int arms) {
      this.kArms = arms;
      return this;
    }

    public BanditBuilder epsilon(Double epsilon) {
      this.epsilon = epsilon;
      return this;
    }

    public BanditBuilder initial(Double initial) {
      this.initial = initial;
      return this;
    }

    public BanditBuilder sampleAverages(Boolean sampleAverages) {
      this.sampleAverages = sampleAverages;
      return this;
    }
  }
}
