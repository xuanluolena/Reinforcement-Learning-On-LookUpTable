package LUTRobot;

import robocode.*;

public class LUTRobotTermialRewardOnly extends LUTRobot {
		public double rewardByTerminal;
		
public void run() {
			
			setAdjustRadarForGunTurn(true);
			setAdjustRadarForRobotTurn(true);
			setAdjustGunForRobotTurn(true);
			turnRadarRight(360);
			
			countBattle = countBattle + 1; 
			state  = lutTable.quantizeDimensions(getHeading(),scannedRobot.getBearing(),scannedRobot.getDistance(),getX(),getY());

			while(true) {
				
				setAdjustRadarForGunTurn(true);
				setAdjustRadarForRobotTurn(true);
				setAdjustGunForRobotTurn(true);
				turnRadarRight(360);
				
				rewardByTerminal = 0;
				
				// take the action that returns the highest Q value
				action = lutTable.selectAction(state[0],state[1],state[2],state[3],state[4],epsilon);		
				this.takeAction(action);   
				
				// get new State
				newState = lutTable.quantizeDimensions(getHeading(),scannedRobot.getBearing(),scannedRobot.getDistance(),getX(),getY());  
				
				// pick up the action that maximize the value function
				newAction = lutTable.selectAction(newState[0],newState[1],newState[2],newState[3],newState[4],0);  	
				
				//get immediate reward
				reward = rewardByTerminal;
				
				// Update Lookup table
				this.updateLUTable(state, action, newState, newAction, reward);   	
				
				state = newState;
			}
		}

			public void onWin(WinEvent event){
				winHistory[countBattle] = 1.0;
			    rewardByTerminal = 3;
			}
			
			public void onDeath(DeathEvent event){
				rewardByTerminal = -3;
			}
	
}
