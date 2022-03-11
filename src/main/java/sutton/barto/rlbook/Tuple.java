package sutton.barto.rlbook;

public class Tuple<A, B> {
  private A first;
  private B second;

  public Tuple(A first, B second) {
    this.first = first;
    this.second = second;
  }

  public A first() {
    return first;
  }

  public Tuple<A, B> setFirst(A first) {
    this.first = first;
    return this;
  }

  public B second() {
    return second;
  }

  public Tuple<A, B> setSecond(B second) {
    this.second = second;
    return this;
  }
}
