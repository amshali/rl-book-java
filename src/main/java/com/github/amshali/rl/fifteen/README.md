## Eight puzzle
This is a smaller/simpler version of 15 puzzle. The idea is to shuffle the numbers in a 3x3 matrix with an empty cell
and bring it to a final configuration which usually looks like this:

```text
|  1|  2|  3|
|  4|  5|  6|
|  7|  8|   |
```
The initial configuration could be for example:

```text
|  4|  2|  7|
|  3|  8|  1|
|  5|   |  6|
```

The code solves this simple version of 15-puzzle using value iteration. It constructs the whole
state space and then evaluates policies.

```shell
mvn clean compile package

java -cp target/Sutton-Barto-RLBook-1.0-jar-with-dependencies.jar \
  com.github.amshali.rl.fifteen.EightPuzzle -th 0.01 -g 0.95
```
The above command runs the puzzle, trains and show some solves.