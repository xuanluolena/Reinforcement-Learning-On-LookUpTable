package LUTRobot;

import java.io.*;
import robocode.*;
import Sarb.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;


public class LUTRobot extends AdvancedRobot implements LUTInterface {
		public double reward,rewardHitByBullet,rewardHitRobot,rewardHitWall,rewardBulletHit;
		
		public static int argHeading = 4;
		public static int argEnemyBearing = 6;
		public static int argDist = 5;
		public static int argPositionX = 4;
		public static int argPositionY = 3;
		public static int argActions = 6;
		
		public static int totalRounds = 4000; // number of rounds	
		public static int threshold = 2000;
		public static int countBattle = -1;   
		public static int action = 0;
		public static int newAction = 0;

		public static double argLearningRate = 0.2;
		public static double argDiscountRate = 0.8;
		public static double epsilon = 0.;// the epsilon in epsilon greedy 

		public static int[] state,newState;

		public static double[] winHistory =new double[totalRounds]; // winning history
		
		public static ScannedRobotEvent scannedRobot;
		public static LUTable lutTable = new LUTable(argHeading, argEnemyBearing, argDist, argPositionX, argPositionY, argActions);
			
		public void run() {
			
			setAdjustRadarForGunTurn(true);
			setAdjustRadarForRobotTurn(true);
			setAdjustGunForRobotTurn(true);
			turnRadarRight(360);
			
			countBattle = countBattle + 1; 
			state  = lutTable.quantizeDimensions(getHeading(),scannedRobot.getBearing(),scannedRobot.getDistance(),getX(),getY());

			while(true) {				

				turnRadarRight(360);
				
				rewardHitByBullet = 0;
				rewardHitRobot = 0;
				rewardHitWall = 0;
				rewardBulletHit = 0;	
				
				// to get the cumulative win rate and turn off exploration after converge
				if(countBattle >= threshold) {
					action = lutTable.selectAction(state[0],state[1],state[2],state[3],state[4],0);		
					this.takeAction(action); 
					state = lutTable.quantizeDimensions(getHeading(),scannedRobot.getBearing(),scannedRobot.getDistance(),getX(),getY());  
					out.println("lala land");
				}
				else {
					// take the action that returns the highest Q value
					action = lutTable.selectAction(state[0],state[1],state[2],state[3],state[4],epsilon);		
					this.takeAction(action);   
					
					// get new State
					newState = lutTable.quantizeDimensions(getHeading(),scannedRobot.getBearing(),scannedRobot.getDistance(),getX(),getY());  
					
					// pick up the action that maximize the value function
					newAction = lutTable.selectAction(newState[0],newState[1],newState[2],newState[3],newState[4],0);  	
					
					//get immediate reward
					reward = rewardHitByBullet + rewardHitRobot + rewardHitWall + rewardBulletHit;
					
					if(countBattle < threshold) {
					// Update Lookup table
						this.updateLUTable(state, action, newState, newAction, reward);   	
					}
					state = newState;
				}
			}
		}
		
		public void onScannedRobot(ScannedRobotEvent e) {
			scannedRobot = e;
			
			// Calculate exact location of the robot
			double absoluteBearing = getHeading() + e.getBearing();
			double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

			// If it's close enough, fire!
			if (Math.abs(bearingFromGun) <= 3) {
				turnGunRight(bearingFromGun);
				// We check gun heat here, because calling fire()
				// uses a turn, which could cause us to lose track
				// of the other robot.
				if (getGunHeat() == 0) {
					fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
				}
			} // otherwise just set the gun to turn.
			// Note:  This will have no effect until we call scan()
			else {
				turnGunRight(bearingFromGun);
			}
			// Generates another scan event if we see a robot.
			// We only need to call this if the gun (and therefore radar)
			// are not turning.  Otherwise, scan is called automatically.
			if (bearingFromGun == 0) {
				scan();
			}
			
		}
		
		// update the lookup table via Q-Learning
		public void updateLUTable(int[] state, int action, int[] newState, int newaction, double reward ){ 
			double oldValue  = lutTable.table[state[0]][state[1]][state[2]][state[3]][state[4]][action]; 
			double newValue  = lutTable.table[newState[0]][newState[1]][newState[2]][newState[3]][newState[4]][newaction];
			double updateValue = oldValue + argLearningRate*(reward+argDiscountRate*newValue-oldValue);
			lutTable.setElement(state[0], state[1], state[2], state[3], state[4], action,updateValue);
		}
		
		public void onHitByBullet(HitByBulletEvent e) {
			rewardHitByBullet = -5 ;
		}

		public void onHitWall(HitWallEvent e) {
	        rewardHitWall = -2;
		}

		public void onHitRobot(HitRobotEvent e){
			rewardHitRobot = -3;
		}

		public void onBulletHit(BulletHitEvent e){
			rewardBulletHit = 4 ;
		}

		public void takeAction(int counter){
			// 6 actions		
			
			switch(counter){
				case (0): {
					ahead(90);
					break;				
				}
				case (1): {
					back(90);					
					break;
				}
				case (2): {
					setAhead(90);
					setTurnLeft(90);
					execute();
					break;
				}
				case (3): {
					setAhead(90);
					setTurnRight(90);
					execute();
					break;
				}
				case (4): {
					setBack(90);
					setTurnLeft(90);
					execute();
					break;
				}
				case (5): {
					setBack(90);
					setTurnRight(90);
					execute();
					break;
				}
			}
				
		}
		
		public void onWin(WinEvent event){
			winHistory[countBattle] = 1.0;  
		}		
		
		// Store information to file system
		public void onBattleEnded(BattleEndedEvent event){
			out.println("End of the battle");
			
			//Save Lookup Table to file system
			File fileLUT = new File("LUT.dat");
			save(fileLUT);
			
			File fileWinRate = new File("winRate.txt");
			saveWinRate(fileWinRate);
			
			File fileWinRate2 = new File("winRate2.txt");
			saveWinRate2(fileWinRate2);
						
			File fileTotalRuns = new File("totalRuns.txt");
			saveTotalRounds(fileTotalRuns);

		}

		@Override
		public double outputFor(double[] X) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double train(double[] X, double argValue) {
			// update the LUT table via learning
			return 0;
		}
		
		// store LUT.dat into the file system
		@Override
		public void save(File argFile) {
			// iterate every element in Lookup table and save them to file system
			try{
				FileWriter writer = new FileWriter(argFile); 
				for (int i1 = 0; i1 < argHeading; i1++){
					for (int i2 = 0; i2 < argEnemyBearing; i2++){
						for (int i3 = 0; i3< argDist;i3++){
							for(int i4 = 0; i4< argPositionX;i4++){
								for(int i5 = 0; i5< argPositionY;i5++){
									for(int i6 = 0; i6< argActions;i6++){
										writer.write(Double.toString(lutTable.getElement(i1, i2, i3, i4, i5, i6))+"\r\n");
									}
								}
							}
						}
					}
				}
				writer.flush();
				writer.close();
			}catch (Exception e){
				out.println("exception in save function");
			}			
			
		}
	
//		// store win rate to file system		
//		public void saveWinRate(File argFile) {
//			
//			// Calculate the total win rate
//			double totalWins = 0;
//			double[] winRate =new double[totalRounds - threshold];
//			double[] totalBattles = new double[totalRounds - threshold];
//
//			for (int i = 0; i<totalRounds - threshold; i++){
//					totalWins = totalWins + winHistory[i + threshold];
//					winRate[i-threshold] = totalWins * 100.0f/(i+1);
//					totalBattles[i] = i+1;
//				}
//			
//			try{			
//				FileWriter writer = new FileWriter(argFile,false); 
//				for (int i = 0; i<totalRounds-threshold; i++){
//					writer.write(Double.toString(winRate[i]/100)+"\r\n");
//					writer.flush();
//				}
//				writer.close();
//			}catch (Exception e){				
//				out.println("exception in saveWinRate function");	
//			}			
//		}
		
		// store win rate to file system		
		public void saveWinRate(File argFile) {
			
			// Calculate the total win rate
			double totalWins = 0;
			double[] winRate =new double[2000];
			double[] totalBattles = new double[2000];

			for (int i = 0; i<2000; i++){
					totalWins = totalWins + winHistory[i+2000];
					winRate[i] = totalWins * 100.0f/(i+1);
					totalBattles[i] = i;
				}
			
			try{			
				FileWriter writer = new FileWriter(argFile,false); 
				for (int i = 0; i<2000; i++){
					writer.write(Double.toString(winRate[i]/100)+"\r\n");
					writer.flush();
				}
				writer.close();
			}catch (Exception e){				
				out.println("exception in saveWinRate function");	
			}			
		}
		
		
		// store win rate for every few rounds to file system		
		public void saveWinRate2(File argFile) {
			// compute the average win rate for every 50 battles
		 	int numforoneRound = 50;
		 	int N2 = totalRounds/numforoneRound;
			double totalWins2 = 0;
			double[] winRate2 =new double[N2];
			double[] totalBattles2 = new double[N2];
			for (int i = 0; i<N2; i++){
				totalWins2 = 0;
				for (int j=0; j<numforoneRound;j++) {
			       totalWins2 = totalWins2 + winHistory[j+i*30];
				}
		       winRate2[i] = totalWins2 * 100.0f/numforoneRound;
		       totalBattles2[i] = i+1;
			}
		 
		 try{  
			    FileWriter writer = new FileWriter(argFile); // write to a .txt. file
			    for (int i = 0; i<N2; i++){
			    	writer.write(Double.toString(winRate2[i]/100)+"\r\n");
			    	writer.flush();
				 }
			    writer.close();
		 	}catch (Exception e){				
				out.println("exception in saveWinRate2 function");	
			}
			
		}
		
		// store the total runs to file system		
		public void saveTotalRounds(File argFile) {	
			try{			
				FileWriter writer = new FileWriter(argFile); 
				for (int i = 0; i<totalRounds; i++){
					writer.write(Integer.toString(i+1)+"\r\n");
					writer.flush();
				}
				writer.close();
			}catch (Exception e){				
				out.println("exception in saveTotalRounds function");	
			}
			
		}
					

		@Override
		public void load(String argFileName) throws IOException {
			return;
		}

		@Override
		public void initialiseLUT() {
			lutTable.initialize();			
		}

		@Override
		public int indexFor(double[] X) {
			// TODO Auto-generated method stub
			return 0;
		}
}
