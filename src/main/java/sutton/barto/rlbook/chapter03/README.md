# Chapter 3

## Grid world

```shell
# Build
mvn clean compile package

# Run:
java -cp target/Sutton-Barto-RLBook-1.0-jar-with-dependencies.jar \
  sutton.barto.rlbook.chapter03.GridWorld -o /tmp/chapter03.html

# Output is an HTML page.
open /tmp/chapter03.html
```

The only confusing part for me was constructing a linear system of equations to solve for figure 3.2. But here is
my try at explaining it. 

The value function is as follows:

![equation](https://latex.codecogs.com/svg.image?v_\pi(s)&space;=&space;\sum_{a}\pi(a&space;|&space;s)&space;\sum_{\acute{s},&space;r}&space;p(\acute{s},&space;r&space;|&space;s,&space;a)\left&space;[&space;r&space;&plus;&space;\gamma&space;v_\pi(\acute{s})&space;\right&space;])

In the case of Grid world, the policy value is `0.25` for any action and state combination.

In addition, 
![equation](https://latex.codecogs.com/svg.image?p(\acute{s},&space;r&space;|&space;s,&space;a)&space;=&space;1)
for all current state, action, next state, reward combinations. 
That is because we deterministically go to a specific state and get a known reward for it.

Moreover, there is only four possible actions, and we go to four possible states
by taking those actions. Hence, we can simply the formula and see how linear 
system of equations is formed. 

![equation](https://latex.codecogs.com/svg.image?v(s)&space;=&space;0.25&space;\left&space;[&space;r^{up}&space;&plus;&space;r^{left}&space;&space;&plus;&space;r^{right}&space;&space;&plus;&space;r^{down}&space;\right&space;]&space;&plus;&space;0.25&space;\gamma&space;\left&space;[&space;v(s^{up})&space;&plus;&space;v(s^{left})&space;&plus;&space;v(s^{right})&space;&plus;&space;v(s^{down})&space;\right&space;])

Which simplifies to:

![eq](https://latex.codecogs.com/svg.image?-0.25&space;\left&space;[&space;r^{up}&space;&plus;&space;r^{left}&space;&space;&plus;&space;r^{right}&space;&plus;&space;r^{down}&space;\right&space;]&space;=&space;-v(s)&space;&plus;&space;0.25&space;\gamma&space;\left&space;[&space;v(s^{up})&space;&plus;&space;v(s^{left})&space;&plus;&space;v(s^{right})&space;&plus;&space;v(s^{down})&space;\right&space;])

The code is trying to construct the above relation with arrays and a matrix.

It's easy to see that the left part is the constant(`b`) and the right side
has the unknowns(value of states) with their coefficients. Given the 
coefficients and the constants we can use a linear solver to find the values(v).
