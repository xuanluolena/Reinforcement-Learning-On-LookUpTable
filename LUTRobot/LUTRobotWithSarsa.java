package LUTRobot;

public class LUTRobotWithSarsa extends LUTRobot {
	public void run() {
		
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		
		countBattle = countBattle + 1; // counter plus one 
		turnRadarRight(360);
		
		state  = lutTable.quantizeDimensions(getHeading(),scannedRobot.getBearing(),scannedRobot.getDistance(),getX(),getY());
		action = lutTable.selectAction(state[0],state[1],state[2],state[3],state[4],epsilon);
		
		while(true) {
			
			rewardHitByBullet = 0;
			rewardHitRobot = 0;
			rewardHitWall = 0;
			rewardBulletHit = 0;
			
			setAdjustRadarForGunTurn(true);
			setAdjustGunForRobotTurn(true);
		
			turnRadarRight(360);
	
			this.takeAction(action);   // Take Action
			
			newState = lutTable.quantizeDimensions(getHeading(),scannedRobot.getBearing(),scannedRobot.getDistance(),getX(),getY());  // Quantization to find the current state
			newAction = lutTable.selectAction(newState[0],newState[1],newState[2],newState[3],newState[4],epsilon);  	// Pick the action according to improve the Q table
			reward = 0;
			reward = rewardHitByBullet + rewardHitRobot + rewardHitWall + rewardBulletHit;
	
			this.updateLUTable(state, action, newState, newAction, reward);   		// Updating
		
			state = newState;
			action = newAction;
			
		}
	}
}
