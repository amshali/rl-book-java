package sutton.barto.rlbook.chapter02;

import com.beust.jcommander.JCommander;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.XYStyler;
import sutton.barto.rlbook.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static sutton.barto.rlbook.Utils.vectorOf;

public class Game {
  public static void main(String[] args) {
    Game game = new Game();
    JCommander.newBuilder()
        .addObject(game)
        .build()
        .parse(args);
    game.run();
  }

  public void run() {
    figure2_2(2000, 1000);
//    figure2_3(2000, 1000);
  }

  private XYChart createChart(int width, int height, String xTitle, String yTitle) {
    final XYChart chart = new XYChartBuilder().width(width).height(height)
            .xAxisTitle(xTitle).yAxisTitle(yTitle).build();
    XYStyler styler = chart.getStyler();
    styler.setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line).setMarkerSize(0);
    styler.setLegendPosition(Styler.LegendPosition.InsideSE);
    styler.setChartTitleVisible(false);
    return chart;
  }

  public void figure2_2(int runs, int time) {
    Vector<Double> epsilons = vectorOf(0.0, 0.1, 0.01);
    List<Bandit> bandits = epsilons.stream()
        .map(eps -> Bandit.builder().epsilon(eps).sampleAverages(true).build()).collect(
            Collectors.toList());
    Tuple<Vector<Vector<Double>>, Vector<Vector<Double>>>
        results = banditSimulation(runs, time, bandits);
    final XYChart rewardsChart = createChart(1000, 500, "Time", "Reward");
    double[] timeAxis = IntStream.range(0, time).mapToDouble(t -> t).toArray();
    IntStream.range(0, bandits.size()).forEach(i -> {
      Vector<Double> bRewards = results.first().get(i);
      double[] rewards = bRewards.stream().mapToDouble(d -> d).toArray();
      rewardsChart.addSeries("ε = " + epsilons.get(i), timeAxis, rewards);
    });
    final XYChart
        bestActionChart = createChart(1000, 500, "Time", "Best action %");
    IntStream.range(0, bandits.size()).forEach(i -> {
      Vector<Double> bActions = results.second().get(i);
      double[] bestActionChoice = bActions.stream().mapToDouble(d -> d * 100).toArray();
      bestActionChart.addSeries("ε = " + epsilons.get(i), timeAxis, bestActionChoice);
    });
    List<XYChart> charts = new ArrayList<>();
    charts.add(rewardsChart);
    charts.add(bestActionChart);
    new SwingWrapper<>(charts, 2, 1).displayChartMatrix();
  }

  public void figure2_3(int runs, int time) {
    Bandit[] bandits = new Bandit[2];
    bandits[0] = Bandit.builder().kArms(10).epsilon(0.0).initial(5.0).build();
    bandits[1] = Bandit.builder().kArms(10).epsilon(0.1).initial(0.0).build();
    Tuple<Vector<Vector<Double>>, Vector<Vector<Double>>>
        results = banditSimulation(runs, time, Arrays.asList(bandits));
    final XYChart
        bestActionChart = createChart(1000, 500, "Time", "Best action %");
    double[] timeAxis = IntStream.range(0, time).mapToDouble(t -> t).toArray();
    IntStream.range(0, bandits.length).forEach(i -> {
      Vector<Double> bActions = results.second().get(i);
      double[] bestActionChoice = bActions.stream().mapToDouble(d -> d * 100).toArray();
      bestActionChart.addSeries("ε = " + bandits[i].epsilon() + ", q = " + bandits[i].initial(),
          timeAxis, bestActionChoice);
    });
    List<XYChart> charts = new ArrayList<>();
    charts.add(bestActionChart);
    new SwingWrapper<>(charts, 1, 1).displayChartMatrix();
  }

  /**
   * Calculates the mean across the last dimension.
   *
   * @return a vector with the mean values.
   */
  public Vector<Double> mean(Vector<Vector<Double>> data, int runs, int time) {
    Vector<Double> result = vectorOf(time, 0.0);
    IntStream.range(0, time).forEach((t) -> {
      Double[] sum = new Double[]{0.0};
      IntStream.range(0, runs).forEach(r -> {
        sum[0] = sum[0] + data.get(r).get(t);
      });
      result.set(t, sum[0] / runs);
    });
    return result;
  }

  public Tuple<Vector<Vector<Double>>, Vector<Vector<Double>>> banditSimulation(int runs, int time,
                                                                                List<Bandit> bandits) {
    Vector<Vector<Vector<Double>>> rewards = vectorOf(bandits.size(), null);
    Vector<Vector<Vector<Double>>> bestActionCount = vectorOf(bandits.size(), null);
    IntStream.range(0, bandits.size()).forEach(i -> {
      Vector<Vector<Double>> banditRuns = vectorOf(runs, null);
      rewards.set(i, banditRuns);
      Vector<Vector<Double>> banditBestActionCount = vectorOf(runs, null);
      bestActionCount.set(i, banditBestActionCount);
      System.out.printf("Bandit %d\n", i);
      Bandit bandit = bandits.get(i);
      IntStream.range(0, runs).forEach(r -> {
        Vector<Double> runTimes = vectorOf(time, 0.0);
        banditRuns.set(r, runTimes);
        Vector<Double> banditAction = vectorOf(time, 0.0);
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
      });
    });
    Vector<Vector<Double>> meanRewards = new Vector<>(bandits.size());
    IntStream.range(0, bandits.size()).forEach(i -> {
      meanRewards.addElement(mean(rewards.get(i), runs, time));
    });
    Vector<Vector<Double>> meanBestActions = new Vector<>(bandits.size());
    IntStream.range(0, bandits.size()).forEach(i -> {
      meanBestActions.addElement(mean(bestActionCount.get(i), runs, time));
    });
    return new Tuple<>(meanRewards, meanBestActions);
  }

}
