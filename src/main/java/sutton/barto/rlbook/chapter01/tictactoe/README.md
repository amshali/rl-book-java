# Tic Tac Toe

This game is written using the *Value function* method. Quoting from the book.
> In the end, evolutionary and value function methods both search the space of policies,
> but learning a value function takes advantage of information available during the course
> of play.

## How to run?

### Compile
To compile run the following command from the root of the project:
```shell
mvn clean compile package
```

### Train
In order to train the model do:
```shell
java -cp target/Sutton-Barto-RLBook-1.0-jar-with-dependencies.jar \
sutton.barto.rlbook.chapter01.tictactoe.Game -m /tmp --train 100000
```
The above command train the player with 100000 epochs and writes the models into
`/tmp` directory.

### Compete
In order to compete the computer players do:
```shell
java -cp target/Sutton-Barto-RLBook-1.0-jar-with-dependencies.jar \
sutton.barto.rlbook.chapter01.tictactoe.Game -m /tmp --compete 1000
```
The above command will read the model from the `/tmp` directory and compete two 
players against each other for 1000 times.

### Play
In order to play against computer do this:
```shell
java -cp target/Sutton-Barto-RLBook-1.0-jar-with-dependencies.jar \
sutton.barto.rlbook.chapter01.tictactoe.Game -m /tmp --play
```
The above command will read the model from the `/tmp` directory and let you play
against computer(Hit Ctrl+C to end the game).
You will be shown a board like this:
```shell
-------------
| 1 | 2 | 3 | 
-------------
| 4 | 5 | 6 | 
-------------
| 7 | 8 | 9 | 
-------------
```
You will have to make a choice of which position you want to play by entering a number from
1 to 9.
