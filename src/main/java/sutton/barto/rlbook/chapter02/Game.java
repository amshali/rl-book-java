package sutton.barto.rlbook.chapter02;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import java.util.List;

public class Game {
  @Parameter(names = {"--explore-rate", "-er"},
      description = "Exploration rate.")
  Double exploreRate = 0.1;

  @Parameter(names = {"--epsilon", "-e"})
  Double epsilon = 0.1;

  @Parameter(names = {"--arms", "-a"})
  Integer arms = 10;

  @Parameter(names = {"--step-size", "-ss"})
  Double stepSize = 0.1;

  public void run() {
  }


  public static void main(String[] args) {
    Game game = new Game();
    JCommander.newBuilder()
        .addObject(game)
        .build()
        .parse(args);
    game.run();
    double[] xData = new double[] {0.0, 1.0, 2.0};
    double[] yData = new double[] {2.0, 1.0, 0.0};

    // Create Chart
    XYChart chart = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)", xData, yData);

    // Show it
    new SwingWrapper(chart).displayChart();
  }

  public void banditSimulation(int nBandits, int time, List<Bandit> bandits) {

  }

}
