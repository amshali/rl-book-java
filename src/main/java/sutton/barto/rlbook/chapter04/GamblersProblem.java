package sutton.barto.rlbook.chapter04;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import sutton.barto.rlbook.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GamblersProblem {
  @Parameter(names = {"--goal", "-g"}, description = "The goal in dollar amount")
  Integer fGoal = 100;
  @Parameter(names = {"--head-prob", "-hp"}, description = "The probability of head")
  Double fHeadProb = 0.4;

  public static void main(String[] args) {
    var gamblersProblem = new GamblersProblem();
    JCommander.newBuilder()
        .addObject(gamblersProblem)
        .build()
        .parse(args);
    gamblersProblem.run();
  }

  private void figure4_3() {
    var states = IntStream.range(0, fGoal + 1).boxed().collect(Collectors.toList());
    var stateValue = states.stream().mapToDouble(i -> 0.0).boxed().collect(Collectors.toList());
    stateValue.set(fGoal, 1.0);
    var sweepHistory = new ArrayList<List<Double>>();
    while (true) {
      var oldStateValue = new ArrayList<>(stateValue);
      sweepHistory.add(oldStateValue);
      IntStream.range(1, fGoal + 1).forEach(state -> {
        var actions = IntStream.range(0, Math.min(state, fGoal - state) + 1);
        var actionReturns = new ArrayList<Double>();
        actions.forEach(a -> actionReturns.add(
            fHeadProb * stateValue.get(state + a) + (1 - fHeadProb) * stateValue.get(state - a)));
        var newValue = actionReturns.stream().max(Comparator.comparingDouble(o -> o)).orElse(-1.0);
        stateValue.set(state, newValue);
      });
      var delta = IntStream.range(0, stateValue.size())
          .mapToDouble(i -> Math.abs(stateValue.get(i) - oldStateValue.get(i))).max().orElse(-1.0);
      if (delta < 1e-9) {
        sweepHistory.add(stateValue);
        break;
      }
    }
    // Compute optimal policy
    var policy = states.stream().mapToInt(i -> 0).boxed().collect(Collectors.toList());
    IntStream.range(1, fGoal + 1).forEach(state -> {
      var actions = IntStream.range(0, Math.min(state, fGoal - state) + 1).toArray();
      var actionReturns = new ArrayList<Double>();
      Arrays.stream(actions).boxed().forEach(a -> actionReturns.add(
          fHeadProb * stateValue.get(state + a) + (1 - fHeadProb) * stateValue.get(state - a)));
      policy.set(state,
          actions[Utils.argmax(
              actionReturns.subList(1,
                      actionReturns.size()).stream().mapToDouble(d -> round(d, 5))
                  .toArray()) + 1]);
    });
    XYChart capital = createChart(900, 500, "Capital", "Final policy (stake)",
        XYSeries.XYSeriesRenderStyle.Step);
    capital.addSeries("Stake", states, policy);
    var charts = new ArrayList<XYChart>();
    charts.add(capital);
    new SwingWrapper<>(charts, 1, 1).displayChartMatrix();
  }

  private Double round(Double d, int decimals) {
    var format = "%%.%df".formatted(decimals);
    return Double.valueOf(format.formatted(d));
  }

  private XYChart createChart(int width, int height, String xTitle, String yTitle,
                              XYSeries.XYSeriesRenderStyle renderStyle) {
    final var chart = new XYChartBuilder().width(width).height(height)
        .xAxisTitle(xTitle).yAxisTitle(yTitle).build();
    var styler = chart.getStyler();
    styler.setDefaultSeriesRenderStyle(renderStyle).setMarkerSize(0);
    styler.setLegendPosition(Styler.LegendPosition.OutsideS);
    styler.setChartTitleVisible(false);
    return chart;
  }

  public void run() {
    figure4_3();
  }
}
