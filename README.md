# cube-solver

A Java project to solve Rubik's cube, built in LeJOS and implemented by a robot constructed from LEGO NXT. The main part of the program is in CubeSolver.java and PCCubeSolver.java.

https://youtu.be/dAfDIDdDclc


## Soft realization

In CubeSolver.java, we implemented all methods of motion the robot needs to solve the cube. It would allow the robot decide whether there is a cube on its platform, and then it would scan the cube by its color sensor, and upload the data of the scrambled cube to PC. 

However, the color sensor of LEGO NXT cannot distinguish orange and red, thus we deploy a neural network to distinguish defferent states of the cube. red.py is a set of training data obtained by me. You may train your color sensor for different cubes and light.

Next, PCCubeSolver.java will come up with a solution of it by a method called Group-Reduce Method. Finally the solution would be downloaded by NXT and it will turn over, rotate, and solve the cube.

### Robot coordinate

Since there are just three output ports of a single NXT, we should come up with a method to handle the cube by only three motors, that is, to use three joints to simulate two hands of people. Thus every time we want to rotate one side of the cube, the robot needs to turn the side downward in advance. Therefore during the whole process the center colors of the cube are consistently altered with respect to the robot (relative positions of them are unchanged when anyone rotates the cube).

In conclusion, we need a robot coordinate to describe the states of the cube. Suppose we are in the position of the color sensor, which is in the left side of the video. Then the front, back, right, left, upward, and downward side of the cube is represented by 0, 1, 2, 3, 4, and 5 in the code.
