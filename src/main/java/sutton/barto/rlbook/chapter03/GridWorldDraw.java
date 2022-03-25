package sutton.barto.rlbook.chapter03;

import j2html.tags.DomContent;
import j2html.tags.specialized.TableTag;
import org.apache.commons.math3.linear.RealMatrix;
import sutton.barto.rlbook.Matrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

import static j2html.TagCreator.*;

public class GridWorldDraw {
  public static TableTag drawMatrix(RealMatrix m, String title) {
    return table(caption(title), tbody(
        IntStream.range(0, m.getRowDimension())
            .<DomContent>mapToObj(i -> tr(IntStream.range(0, m.getColumnDimension())
                .<DomContent>mapToObj(
                    j -> td("%.2f".formatted(m.getEntry(i, j))))
                .toArray(DomContent[]::new))).toArray(DomContent[]::new))).withClass("matrix");
  }

  public static <T> TableTag drawPolicy(Matrix<T> m, String title) {
    return table(caption(title), tbody(
        IntStream.range(0, m.rows())
            .<DomContent>mapToObj(i -> tr(IntStream.range(0, m.columns())
                .<DomContent>mapToObj(
                    j -> td(m.get(i, j).toString())).toArray(DomContent[]::new)))
            .toArray(DomContent[]::new))).withClass("matrix");
  }

  public static void generateHtml(File output, DomContent... content) throws IOException {
    var htmlString = html(head(style("""
            body {
              font-size: large;
              font-family: monospace;
            }
            .matrix {
              margin-top: 1em;
              border-collapse: collapse;
              border: 2px solid black;
            }
            .matrix td {
              text-align: center;
              border: 2px solid black;
              padding: 1em;
            }
        """)), body(content)).render();
    try (var fos = new FileOutputStream(output)) {
      fos.write(htmlString.getBytes(StandardCharsets.UTF_8));
    }
  }
}
