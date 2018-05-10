package LUTRobot;

import java.util.*;


public class LUTable{
    public double table[][][][][][];
	int argHeading;
	int argEnemyBearing;
	int argDist;
	int argPositionX;
	int argPositionY;
	int argActions;

	//constructor
	public LUTable(int argHeading , int argEnemyBearing, int argDist, int argPositionX, int argPositionY, int argActions){
		this.setTable(argHeading , argEnemyBearing, argDist, argPositionX, argPositionY, argActions);
		this.initialize();
	}
	
	public void setTable(int argHeading , int argEnemyBearing, int argDist, int argPositionX, int argPositionY, int argActions){
		// set the parameter
		this.argHeading = argHeading;
		this.argEnemyBearing = argEnemyBearing;
		this.argDist = argDist;
		this.argPositionX = argPositionX;
		this.argPositionY = argPositionY;
		this.argActions = argActions;
	}
	
	public void setElement(int argHeading , int argEnemyBearing, int argDist, int argPositionX, int argPositionY, int argActions, double newValue){
		table[argHeading][argEnemyBearing][argDist][argPositionX][argPositionY][argActions] = newValue;
	}
	
	public double getElement(int argHeading , int argEnemyBearing, int argDist, int argPositionX, int argPositionY, int argActions){
		return table[argHeading][argEnemyBearing][argDist][argPositionX][argPositionY][argActions];
	}
	/*Initialize the look up table to 0  */
	public void initialize() {
		this.table = new double[this.argHeading][this.argEnemyBearing][this.argDist][this.argPositionX][this.argPositionY][this.argActions];

		for (int i1 = 0; i1 < this.argHeading; i1++){
			for (int i2 = 0; i2 < this.argEnemyBearing; i2++){
				for (int i3 = 0; i3< this.argDist;i3++){
					for(int i4 = 0; i4< this.argPositionX;i4++){
						for(int i5 = 0; i5< this.argPositionY;i5++){
							for(int i6 = 0; i6< this.argActions;i6++){
									table[i1][i2][i3][i4][i5][i6] = 0;
								}
							}
						}
					}
				}
			}
		}

	//quantize dimensions of heading, enemy bearing, distance, postion of X and Y
	
	public int[] quantizeDimensions(double argHeading, double argEnemyBearing, double argDist, double argPositionX, double argPositionY){
		
		int[] result=new int[5];

		result[0] = this.quantizeHeading(argHeading);
		result[1] = this.quantizeEnemyBearing(argEnemyBearing);
		result[2] = this.quantizeDistance(argDist);
		result[3]=  this.quantizeXlabel(argPositionX);
		result[4] = this.quantizeYlabel(argPositionY);

		return result;
	}
	
	public int quantizeHeading(double robotHeading){
		double angle = 360 / this.argHeading;
		int result = (int)(robotHeading/angle);
		if(result >= this.argHeading)
			result = 0;
		return result;
	}
	public int quantizeEnemyBearing(double e){
		double angle = 360 / this.argEnemyBearing;
		int result = (int)((180 + e)/angle);
		if(result >= this.argEnemyBearing)
			result = 0;
		return result;
	}
	public int quantizeDistance(double e){
		return Math.min((int) (e/100.0),this.argDist-1);
	}
	public int quantizeXlabel(double x){
		return Math.min((int)(x/200),this.argPositionX-1);
	}
	public int quantizeYlabel(double y){
		return Math.min((int)(y/200), this.argPositionY-1);
	}


// find the action number corresponding to the maximum value in the state-action pair table corresponding to a certain state
	public int maxValue(int i, int j, int k, int m, int n){
		double max =  -100;
		int counter1 = 0;
		int action = 0;
		double current = 0.0;
		for (counter1 = 0; counter1 < this.argActions; counter1++){
			current = this.getElement(i, j, k, m, n, counter1);
			if (current > max){
				max = current;
				action= counter1;
			}		   
		}

		return action;
	}

	// select the action according to epsilon greedy
	public int selectAction(int i, int j, int k, int m, int n, double epsilon){
		int result = 0;
		Random generator1 = new Random();
		Random generator2 = new Random();
		int temp = this.maxValue(i, j, k, m, n);
		if(generator1.nextDouble() < epsilon){
			result = generator2.nextInt(this.argActions);
			System.out.println("result and temp are");
			System.out.println(result);
			System.out.println(temp);
			
		}
		else{
			result = this.maxValue(i, j, k, m, n);
		}
		return result;
	}
}

