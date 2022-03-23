package sutton.barto.rlbook.chapter02;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;
import me.tongfei.progressbar.ProgressBar;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import sutton.barto.rlbook.Utils;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static sutton.barto.rlbook.Utils.vectorOf;

public class Game {

  @Parameter(names = {"--figure", "-f"}, description = "Figure to show")
  String fFigure;
  @Parameter(names = {"--runs", "-r"}, validateWith = PositiveInteger.class,
      description = "Number of runs(epochs)")
  Integer fRuns = 2000;

  @Parameter(names = {"--steps", "-s"}, validateWith = PositiveInteger.class,
      description = "Number of steps in each run(epoch)")
  Integer fSteps = 1000;

  public static void main(String[] args) {
    var game = new Game();
    JCommander.newBuilder()
        .addObject(game)
        .build()
        .parse(args);
    game.run();
  }

  public void run() {
    switch (fFigure) {
      case "figure2_2" -> figure2_2(fRuns, fSteps);
      case "figure2_3" -> figure2_3(fRuns, fSteps);
      case "figure2_4" -> figure2_4(fRuns, fSteps);
      case "figure2_5" -> figure2_5(fRuns, fSteps);
      case "figure2_6" -> figure2_6(fRuns, fSteps);
      default -> {
        System.err.printf("Invalid flag value: %s\n", fFigure);
        System.exit(-1);
      }
    }
  }

  private XYChart createChart(int width, int height, String xTitle, String yTitle) {
    final var chart = new XYChartBuilder().width(width).height(height)
        .xAxisTitle(xTitle).yAxisTitle(yTitle).build();
    var styler = chart.getStyler();
    styler.setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line).setMarkerSize(0);
    styler.setLegendPosition(Styler.LegendPosition.OutsideS);
    styler.setChartTitleVisible(false);
    return chart;
  }

  public void figure2_2(int runs, int time) {
    var epsilons = vectorOf(0.0, 0.1, 0.01);
    var bandits = epsilons.stream()
        .map(eps -> Bandit.builder().epsilon(eps).sampleAverages(true).build()).collect(
            Collectors.toList());
    var results = banditSimulation(runs, time, bandits);
    final var rewardsChart = createChart(800, 500, "Time", "Reward");
    var timeAxis = IntStream.range(0, time).mapToDouble(t -> t).toArray();
    IntStream.range(0, bandits.size()).forEach(i -> {
      var bRewards = results.get("mean_rewards").get(i);
      double[] rewards = bRewards.stream().mapToDouble(d -> d).toArray();
      rewardsChart.addSeries("ε = " + epsilons.get(i), timeAxis, rewards);
    });
    final var bestActionChart = createChart(800, 500, "Time", "Best action %");
    IntStream.range(0, bandits.size()).forEach(i -> {
      Vector<Double> bActions = results.get("mean_best_action_fraction").get(i);
      double[] bestActionChoice = bActions.stream().mapToDouble(d -> d * 100).toArray();
      bestActionChart.addSeries("ε = " + epsilons.get(i), timeAxis, bestActionChoice);
    });
    var charts = new ArrayList<XYChart>();
    charts.add(rewardsChart);
    charts.add(bestActionChart);
    new SwingWrapper<>(charts, 2, 1).displayChartMatrix();
  }

  public void figure2_3(int runs, int time) {
    var bandits = new Bandit[3];
    bandits[0] = Bandit.builder().kArms(10).epsilon(0.0).initial(5.0).build();
    bandits[1] = Bandit.builder().kArms(10).epsilon(0.1).initial(0.0).build();
    bandits[2] = Bandit.builder().kArms(10).epsilon(0.1).initial(5.0).build();
    var results = banditSimulation(runs, time, Arrays.asList(bandits));
    final var bestActionChart = createChart(800, 500, "Time", "Best action %");
    var timeAxis = IntStream.range(0, time).mapToDouble(t -> t).toArray();
    IntStream.range(0, bandits.length).forEach(i -> {
      var bActions = results.get("mean_best_action_fraction").get(i);
      var bestActionChoice = bActions.stream().mapToDouble(d -> d * 100).toArray();
      bestActionChart.addSeries("ε = " + bandits[i].epsilon() + ", q = " + bandits[i].initial(),
          timeAxis, bestActionChoice);
    });
    var charts = new ArrayList<XYChart>();
    charts.add(bestActionChart);
    new SwingWrapper<>(charts, 1, 1).displayChartMatrix();
  }

  public void figure2_4(int runs, int time) {
    var bandits = new ArrayList<Bandit>();
    bandits.add(Bandit.builder().kArms(10).epsilon(0.0).sampleAverages(true).ucbParam(2.0).build());
    bandits.add(Bandit.builder().kArms(10).epsilon(0.1).sampleAverages(true).build());
    var results = banditSimulation(runs, time, bandits);
    final var rewardsChart = createChart(800, 500, "Time", "Reward");
    var timeAxis = IntStream.range(0, time).mapToDouble(t -> t).toArray();
    IntStream.range(0, bandits.size()).forEach(i -> {
      var bRewards = results.get("mean_rewards").get(i);
      double[] rewards = bRewards.stream().mapToDouble(d -> d).toArray();
      rewardsChart.addSeries(
          "ε = %.2f, UCB = %.2f".formatted(bandits.get(i).epsilon(), bandits.get(i).ucbParam()),
          timeAxis, rewards);
    });
    var charts = new ArrayList<XYChart>();
    charts.add(rewardsChart);
    new SwingWrapper<>(charts, 1, 1).displayChartMatrix();
  }

  public void figure2_5(int runs, int time) {
    var bandits = new ArrayList<Bandit>();
    bandits.add(
        Bandit.builder().gradient(true).gradientBaseline(true).alpha(0.1).trueReward(4.0)
            .build());
    bandits.add(
        Bandit.builder().gradient(true).gradientBaseline(false).alpha(0.1).trueReward(4.0)
            .build());
    bandits.add(
        Bandit.builder().gradient(true).gradientBaseline(true).alpha(0.4).trueReward(4.0)
            .build());
    bandits.add(
        Bandit.builder().gradient(true).gradientBaseline(false).alpha(0.4).trueReward(4.0)
            .build());
    var results = banditSimulation(runs, time, bandits);
    var timeAxis = IntStream.range(0, time).mapToDouble(t -> t).toArray();
    final var bestActionChart = createChart(800, 500, "Time", "Best action %");
    IntStream.range(0, bandits.size()).forEach(i -> {
      Vector<Double> bActions = results.get("mean_best_action_fraction").get(i);
      double[] bestActionChoice = bActions.stream().mapToDouble(d -> d * 100).toArray();
      bestActionChart.addSeries(
          "α = %.2f, baseline = %s".formatted(bandits.get(i).alpha(),
              bandits.get(i).gradientBaseline()),
          timeAxis, bestActionChoice);
    });
    var charts = new ArrayList<XYChart>();
    charts.add(bestActionChart);
    new SwingWrapper<>(charts, 1, 1).displayChartMatrix();
  }

  public void figure2_6(int runs, int time) {
    var labels = Map.of("epsilon", "ε-greedy", "alpha",
        "gradient bandit", "coef", "UCB", "initial", "optimistic initialization"
    );
    var labelColors = Map.of("epsilon", Color.RED, "alpha",
        Color.GREEN, "coef", Color.BLUE, "initial", Color.BLACK
    );
    var generators = new HashMap<String, Function<Double, Bandit>>();
    generators.put("epsilon",
        (Double epsilon) -> Bandit.builder().epsilon(epsilon).sampleAverages(true).build());
    generators.put("alpha",
        (Double alpha) -> Bandit.builder().alpha(alpha).gradient(true).gradientBaseline(true)
            .build());
    generators.put("coef",
        (Double coef) -> Bandit.builder().epsilon(0.0).ucbParam(coef).sampleAverages(true).build());
    generators.put("initial",
        (Double initial) -> Bandit.builder().epsilon(0.0).initial(initial).alpha(0.1).build());

    var parametersMap = Map.of("epsilon", IntStream.range(-7, -1).toArray(),
        "alpha", IntStream.range(-5, 2).toArray(),
        "coef", IntStream.range(-4, 3).toArray(),
        "initial", IntStream.range(-2, 3).toArray()
    );
    var bandits = new ArrayList<Bandit>();
    generators.keySet().stream().sorted().forEach((name) -> {
      var params = parametersMap.get(name);
      Arrays.stream(params).asDoubleStream().forEach(d -> {
        bandits.add(generators.get(name).apply(Math.pow(2, d)));
      });
    });
    var results = banditSimulation(runs, time, bandits);
    var rewards = mean(results.get("mean_rewards"), 2);
    AtomicInteger i = new AtomicInteger();
    var charts = new ArrayList<XYChart>();
    final var chart = createChart(800, 500, "Param", "Reward");
    labels.keySet().stream().sorted().forEach(label -> {
      var params = parametersMap.get(label);
      var yData =
          rewards.subList(i.get(), i.get() + params.length).stream().mapToDouble(d -> d).toArray();
      var xAxisData = new TreeSet<Integer>();
      Arrays.stream(params).forEach(xAxisData::add);
      var xAxis = xAxisData.stream().mapToDouble(d -> d).toArray();
      chart.addSeries("%s".formatted(labels.get(label)), xAxis, yData);
      i.addAndGet(params.length);
    });
    var colors = new ArrayList<Color>();
    labels.keySet().stream().sorted().forEach(l -> colors.add(labelColors.get(l)));
    chart.getStyler().setSeriesColors(colors.toArray(new Color[0]));
    charts.add(chart);
    new SwingWrapper<>(charts, 1, 1).displayChartMatrix();
  }

  /**
   * Calculates the mean across the last dimension.
   *
   * @param data all the rows must be of the same size.
   * @param axis possible values: 1, 2. 1 means we calculate the mean over the first axis. If data
   *             is a matrix then this means columnar calculation of mean. Axis 2 means
   *             calculating the mean row-wise.
   * @return a vector with the mean values.
   */
  public Vector<Double> mean(Vector<Vector<Double>> data, int axis) {
    var result = new Vector<Double>();
    var axis1Size = data.size();
    var axis2Size = data.get(0).size();
    IntStream.range(0, axis == 1 ? axis2Size : axis1Size).forEach((i) -> {
      var sum = new Double[]{0.0};
      IntStream.range(0, axis == 1 ? axis1Size : axis2Size)
          .forEach(j -> sum[0] += data.get(axis == 1 ? j : i).get(axis == 1 ? i : j));
      result.add(sum[0] / (axis == 1 ? axis1Size : axis2Size));
    });
    return result;
  }

  public Map<String, Vector<Vector<Double>>> banditSimulation(int runs, int time,
                                                              List<Bandit> bandits) {
    var rewards = Utils.<Vector<Vector<Double>>>vectorOf(bandits.size(), null);
    var bestActionCount = Utils.<Vector<Vector<Double>>>vectorOf(bandits.size(), null);
    var pb = new ProgressBar("Bandits runs", (long) bandits.size() * runs);
    IntStream.range(0, bandits.size()).parallel().forEach(i -> {
      var banditRuns = Utils.<Vector<Double>>vectorOf(runs, null);
      rewards.set(i, banditRuns);
      var banditBestActionCount = Utils.<Vector<Double>>vectorOf(runs, null);
      bestActionCount.set(i, banditBestActionCount);
      var bandit = bandits.get(i);
      IntStream.range(0, runs).forEach(r -> {
        var runTimes = vectorOf(time, 0.0);
        banditRuns.set(r, runTimes);
        var banditAction = vectorOf(time, 0.0);
        banditBestActionCount.set(r, banditAction);
        bandit.reset();
        IntStream.range(0, time).forEach(t -> {
          int action = bandit.getAction();
          double reward = bandit.takeAction(action);
          runTimes.set(t, reward);
          if (action == bandit.bestAction()) {
            banditAction.set(t, 1.0);
          } else {
            banditAction.set(t, 0.0);
          }
        });
        pb.step();
      });
    });
    var meanRewards = new Vector<Vector<Double>>(bandits.size());
    IntStream.range(0, bandits.size()).forEach(i -> {
      meanRewards.addElement(mean(rewards.get(i), 1));
    });
    var meanBestActions = new Vector<Vector<Double>>(bandits.size());
    IntStream.range(0, bandits.size()).forEach(i -> {
      meanBestActions.addElement(mean(bestActionCount.get(i), 1));
    });
    return Map.of("mean_rewards", meanRewards, "mean_best_action_fraction", meanBestActions);
  }

}
