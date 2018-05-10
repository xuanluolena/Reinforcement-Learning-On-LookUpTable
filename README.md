# Reinforcement-Learning-On-Robocode

An overview of all the classes:
1, Parent class LUTRobot is the implementation of Q-Learing with intermediate reward. 
2, Child class LUTRobotTermialRewardOnly is the implementation of Q-Learing with terminal reward only.  
3, Child class LUTRobotWithSarsa is the implementation of Sarsa.  
4, Class LUTable is the implementation of lookup table.   

The purpose of the implementation:  
In this part we implement Reinforcement Learning for my robot tank based on Robocode environment. Before implementation, the following
needs to be decided:  
1, which actions I would like to support  
2, when & how to generate rewards.  

Then I capture the contents of a trained LUT to file and this will be later helpful when replacing the LUT component with a neural net. 
