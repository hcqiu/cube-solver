# cube-solver

A Java project to solve Rubik's cube, built in LeJOS and implemented by a robot constructed from LEGO NXT. The main part of the program is in CubeSolver.java and PCCubeSolver.java.

https://youtu.be/dAfDIDdDclc


## Soft realization

In CubeSolver.java, we implemented all methods of motion the robot needs to solve the cube. It would allow the robot decide whether there is a cube on its platform, and then it would scan the cube by its color sensor, and upload the data of the scrambled cube to PC. 

However, the color sensor of LEGO NXT cannot distinguish orange and red, thus we deploy a neural network to distinguish defferent states of the cube. red.py is a set of training data obtained by me. You may train your color sensor for different cubes and light.

Next, PCCubeSolver.java will come up with a solution of it by a method called Group-Reduce Method. Finally the solution would be downloaded by NXT and it will turn over, rotate, and solve the cube.

### Robot coordinate

