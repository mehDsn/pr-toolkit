package optimization.stopCriteria;

import optimization.gradientBasedMethods.Objective;
import util.ArrayMath;
import util.MathUtil;

public class AverageValueDifference implements StopingCriteria{
	
	/**
	 * Stop if the different between values is smaller than a treshold
	 */
	protected double valueConvergenceValue=10E-6;
	protected double previousValue = Double.NaN;
	protected double currentValue = Double.NaN;
	
	public AverageValueDifference(double valueConvergenceValue){
		this.valueConvergenceValue = valueConvergenceValue;
	}
	
	public void reset(){
		previousValue = Double.NaN;
		currentValue = Double.NaN;
	}

	
	public boolean stopOptimization(Objective obj){
		if(ArrayMath.L2Norm(obj.gradient) == 0){
			System.out.println("Value Stop: Gradient is zero");
			return true;
		}
		if(Double.isNaN(currentValue)){
			currentValue = obj.getValue();
			return false;
		}else {
			previousValue = currentValue;
			currentValue = obj.getValue();
			double valueDiff = Math.abs(previousValue - currentValue);
			double valueAverage = Math.abs(previousValue + currentValue)/2;	
			if( valueDiff/valueAverage  < valueConvergenceValue){
				System.out.println("Value Stop: "  
						+ valueDiff + " avg: " + valueAverage  
						+ " res: " + valueDiff/valueAverage);
				return true;
			}
		}
		return false;
	}
}
