/*
 */
package hfk;

import hfk.game.GameController;

/**
 * Random generator with a custom distribution that is very similar to the
 * exponential distribution, but this one is finite.
 * It produces doubles in the interval [0,1].
 * The cumulative distribution function F(x) is:
 * in case of x < 0: 0
 * in case of x > 1: 1
 * in case of 0 < x < 1: a * (1 - exp(-L * x))
 * a scales F so that F(1) == 1.
 * a and L (lambda) are calculated in the constructor.
 * @author LostMekka
 */
public class ExpRandom {
	
	private final double p;
	private final double a;
	private final double negativeLambda;

	public ExpRandom() {
		this(0.9);
	}
	
	/**
	 * Creates an ExpRandom object with a specific distribution function.
	 * Note that p == 0.5 would create a uniform distribution.
	 * @param p The probability of the numbers generated being less or equal than 0.5
	 */
	public ExpRandom(double p) {
		if(p <= 0.5 || p >= 1) throw new RuntimeException("p outside bounds!");
		this.p = p;
		a = p / (2 - 1 / p);
		negativeLambda = Math.log(1 - 1 / a);
	}
	
	public double getNextFloat(){
		return (float)getNextDouble();
	}
	
	public double getNextDouble(){
		return Math.log(1 - GameController.random.nextDouble() / a) / negativeLambda;
	}
	
	public double getNextInt(int min, int max){
		// to make max inclusive, the max double value is actually 1 greater.
		// the probability of getNextDouble actually returning (max + 1) is
		// ridiculously low.
		// but better to be safe and cap the returned value to max
		return Math.min(max, (int)getNextDouble(min, max + 1));
	}
	
	public double getNextFloat(double min, double max){
		return (float)getNextDouble(min, max);
	}
	
	public double getNextDouble(double min, double max){
		return min + (max - min) * getNextDouble();
	}
	
}
