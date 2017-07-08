/**
 * MIT License
 * 
 * Copyright (c) 2017 Justin Kunimune
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package util;

import java.util.Arrays;

/**
 * A whole class just for numeric approximation methods
 * 
 * @author jkunimune
 */
public class NumericalAnalysis {

	public static final void main(String[] args) {
		System.out.println(simpsonIntegrate(-1,1, (x,prms) -> 4*Math.sqrt(1-x*x), .001, null));
	}
	/**
	 * Performs a definite integral using Simpson's rule and a constant step size
	 * @param a The start of the integration region
	 * @param b The end of the integration region (must be greater than a)
	 * @param f The integrand
	 * @param h The step size (must be positive)
	 * @param constants Constant parameters for the function
	 * @return \int_a^b \! f(x) \, \mathrm{d}x
	 */
	public static final double simpsonIntegrate(double a, double b, ScalarFunction f, double h, double... constants) {
		double sum = 0;
		for (double x = a; x < b; x += h) {
			if (x+h > b) 	h = b-x;
			sum += h/6*(f.evaluate(x,constants)
					+ 4*f.evaluate(x+h/2, constants)
					+   f.evaluate(x+h, constants));
		}
		return sum;
	}
	
	
	/**
	 * Solves a simple ODE using Simpson's rule and a constant step size
	 * @param T The maximum time value at which to sample (must be positive)
	 * @param n The desired number of spaces (or the number of samples minus 1)
	 * @param f The derivative of y with respect to time
	 * @param h The internal step size (must be positive)
	 * @param constants Constant parameters for the function
	 * @return the double[] y, where y[i] is the value of y at t=i*T/n
	 */
	public static final double[] simpsonODESolve(double T, int n, ScalarFunction f, double h, double... constants) {
		final double[] y = new double[n+1]; //the output
		double t = 0;
		double sum = 0;
		for (int i = 0; i <= n; i ++) {
			while (t < i*T/n) {
				final double tph = Math.min(t+h, i*T/n);
				sum += (tph-t)/6*(f.evaluate(t, constants)
							  + 4*f.evaluate((t+tph)/2, constants)
							  +   f.evaluate(tph, constants));
				t = tph;
			}
			y[i] = sum;
		}
		return y;
	}
	
	
	/**
	 * Applies Newton's method in one dimension to solve for x such that f(x)=y
	 * @param y Desired value for f
	 * @param x0 Initial guess for x
	 * @param f The error in terms of x
	 * @param dfdx The derivative of f with respect to x
	 * @param tolerance The maximum error that this can return
	 * @param constants Constant parameters for the function
	 * @return The value of x that puts f near 0, or NaN if it does not
	 * 		converge in 5 iterations
	 */
	public static final double newtonRaphsonApproximation(
			double y, double x0, ScalarFunction f, ScalarFunction dfdx,
			double tolerance, double... constants) {
		double x = x0;
		double error = Math.PI;
		for (int i = 0; i < 5 && error > tolerance; i ++) {
			error = f.evaluate(x, constants) - y;
			final double dydx = dfdx.evaluate(x, constants);
			x -= error/dydx;
		}
		if (error > tolerance)
			return Double.NaN;
		else
			return x;
	}
	
	
	/**
	 * Applies Newton's method in two dimensions to solve for phi and lam such
	 * that f1(phi,lam)=x and f2(phi,lam)=y
	 * @param x Desired value for f1
	 * @param y Desired value for f2
	 * @param phi0 Initial guess for phi
	 * @param lam0 Initial guess for lam
	 * @param f1 x-error in terms of phi and lam
	 * @param f2 y-error in terms of phi and lam
	 * @param df1dp The partial derivative of x with respect to phi
	 * @param df1dl The partial derivative of x with respect to lam
	 * @param df2dp The partial derivative of y with respect to phi
	 * @param df2dl The partial derivative of y with respect to lam
	 * @param tolerance The maximum error that this can return
	 * @param constants Constant parameters for the functions
	 * @return the values of phi and lam that put f1 and f2 near 0, or
	 * 			<code>null</code> if it does not converge in 5 iterations.
	 */
	public static final double[] newtonRaphsonApproximation(double x, double y,
			double phi0, double lam0, VectorFunction f1, VectorFunction f2,
			VectorFunction df1dp, VectorFunction df1dl, VectorFunction df2dp,
			VectorFunction df2dl, double tolerance, double... constants) {
		double phi = phi0;
		double lam = lam0;
		double error = Math.PI;
		
		for (int i = 0; i < 5 && error > tolerance; i++) {
			final double F1mx = f1.evaluate(phi, lam, constants) - x;
			final double F2my = f2.evaluate(phi, lam, constants) - y;
			final double dF1dP = df1dp.evaluate(phi, lam, constants);
			final double dF1dL = df1dl.evaluate(phi, lam, constants);
			final double dF2dP = df2dp.evaluate(phi, lam, constants);
			final double dF2dL = df2dl.evaluate(phi, lam, constants);
			
			phi -= (F1mx*dF2dL - F2my*dF1dL) / (dF1dP*dF2dL - dF2dP*dF1dL);
			lam -= (F2my*dF1dP - F1mx*dF2dP) / (dF1dP*dF2dL - dF2dP*dF1dL);
			
			error = Math.hypot(F1mx, F2my);
		}
		
		if (error > tolerance) // if it aborted due to timeout
			return null;
		else // if it aborted due to convergence
			return new double[] {phi, lam};
	}
	
	
	/**
	 * Applies aitken interpolation to an array of tabulated values
	 * @param x The input value
	 * @param X The sorted array of inputs on which to interpolate
	 * @param f The sorted array of outputs on which to interpolate
	 * @return f(x), approximately
	 */
	public static final double aitkenInterpolate(double x, double[] X, double[] f) {
		return aitkenInterpolate(x, X, f, 0, X.length);
	}
	
	/**
	 * Applies aitken interpolation to a subset of an array of tabulated values
	 * @param x The input value
	 * @param X The sorted array of inputs on which to interpolate
	 * @param f The sorted array of outputs on which to interpolate
	 * @param from The index of the arrays at which to start (inclusive)
	 * @param to The index of the arrays at which to stop (exclusive)
	 * @return f(x), approximately
	 */
	public static final double aitkenInterpolate(double x,
			double[] X, double[] f, int from, int to) { //map from X to f using elements start to end
		final int N = to - from;
		final double[][] fx = new double[N][]; // the table of successive approximations
		
		fx[0] = Arrays.copyOfRange(f, from, to); //fill in the zeroth row
		
		for (int i = 1; i < N; i ++) { //i+1 is the number of points interpolated on
			fx[i] = new double[N];
			for (int j = i; j < N; j ++) { //the points will be 0, ..., i-1, j
				fx[i][j] = 1/(X[from+j] - X[from+i-1])*Math2.determ(
						fx[i-1][i-1], fx[i-1][j],
						X[from+i-1] - x, X[from+j] - x); //I won't bother to explain this; go look up Aitken interpolation
			}
		}
		
		return fx[N-1][N-1];
	}
	
	
	
	@FunctionalInterface
	public interface ScalarFunction {
		public double evaluate(double x, double[] constants);
	}
	
	@FunctionalInterface
	public interface VectorFunction {
		public double evaluate(double x, double y, double[] constants);
	}

}