package sutton.barto.rlbook;

public class Tuple<A, B> {
  private final A first;
  private final B second;

  public Tuple(A first, B second) {
    this.first = first;
    this.second = second;
  }

  public A first() {
    return first;
  }

  public B second() {
    return second;
  }
}
