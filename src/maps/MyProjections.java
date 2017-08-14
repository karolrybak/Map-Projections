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
package maps;

import org.apache.commons.math3.complex.Complex;

/**
 * All of the projections I invented, save the tetrahedral ones,
 * because those have so much in common with other tetrahedral projections.
 * 
 * @author jkunimune
 */
public class MyProjections {
	
	public static final Projection MAGNIFIER =
			new Projection("Magnifier", "A novelty map projection that blows up the center way out of proportion",
					1., 0b1011, "azimuthal", "pointless") {
		
		public double[] project(double lat, double lon) {
			final double p = 1/2.0+lat/Math.PI;
			final double fp = 1 - 0.1*p - 0.9*Math.pow(p,7);
			final double r = Math.PI*fp;
			return new double[] { r*Math.sin(lon), -r*Math.cos(lon) };
		}
		
		public double[] inverse(double x, double y) {
			double R = Math.hypot(x, y);
			if (R <= 1)
				return new double[] {
						Math.PI/2 * (1 - R*.2 - R*R*R*1.8),
						Math.atan2(y, x) + Math.PI/2};
			else
				return null;
		}
	};
	
	
	public static final Projection EXPERIMENT =
			new Projection("Experiment", "What happens when you apply a complex differentiable function to a stereographic projection?",
					1., 0b0000, "?", "conformal") {
		
		public double[] project(double lat, double lon) {
			final double wMag = Math.tan(Math.PI/4-lat/2);
			final Complex w = new Complex(wMag*Math.sin(lon), -wMag*Math.cos(lon));
			Complex z = w.asin();
			if (z.isInfinite() || z.isNaN())	z = new Complex(0);
			return new double[] { z.getReal(), z.getImaginary() };
		}
		
		public double[] inverse(double x, double y) {
			Complex z = new Complex(x*Math.PI, y*Math.PI);
			Complex ans = z.sin();
			double p = 2 * Math.atan(ans.abs());
			double theta = ans.getArgument();
			double lambda = Math.PI/2 - p;
			return new double[] {lambda, theta};
		}
	};
	
	
	
	public static final Projection PSEUDOSTEREOGRAPHIC =
			new Projection("Pseudostereographic", "The logical next step after Aitoff and Hammer",
					2, 0b1111, "pseudocylindrical", "compromise") {
		
		public double[] project(double lat, double lon) {
			double[] transverse = Azimuthal.STEREOGRAPHIC.project(
					obliquifySphc(lat, lon/2, new double[] {0,0,0}));
			return new double[] {2*transverse[0], transverse[1]};
		}
		
		public double[] inverse(double x, double y) {
			double[] transverse = obliquifyPlnr(
					Azimuthal.STEREOGRAPHIC.inverse(x/2, y/2), new double[] {0,0,0});
			if (transverse == null) 	return null;
			else 	return new double[] {transverse[0], 2*transverse[1]};
		}
	};
	
	
	public static final Projection HYPERELLIPOWER =
			new Projection("Hyperellipower", "A parametric projection that I'm still testing",
					2., 0b1111, "pseudocylindrical", "compromise", new String[] {"k","n","a"},
					new double[][] {{1,5,4.99},{.5,2.,1.20},{.5,2.,1.13}}) {
		
		private double k, n, a;
		
		public void setParameters(double... params) {
			this.k = params[0];
			this.n = params[1];
			this.a = params[2];
			this.aspectRatio = 2*Math.sqrt(n)/a;
		}
		
		public double[] project(double lat, double lon) {
			final double ynorm = (1-Math.pow(1-Math.abs(lat/(Math.PI/2)), n));
			return new double[] {
					Math.pow(1 - Math.pow(ynorm, k),1/k)*lon,
					ynorm*Math.PI/2/Math.sqrt(n)*a*Math.signum(lat) };
		}
		
		public double[] inverse(double x, double y) {
			return new double[] {
					(1 - Math.pow(1-Math.abs(y), 1/n))*Math.PI/2*Math.signum(y),
					x/Math.pow(1 - Math.pow(Math.abs(y),k),1/k)*Math.PI };
		}
	};
	
	
	public static final Projection TWO_POINT_EQUALIZED =
			new Projection("Two-Point Equalized", "A paramatric projection that I'm still testing",
					0, 0b1111, "other", "compromise", new String[] {"Width"},
					new double[][] { {0, 180, 90} }) {
		
		private double theta;
		
		public void setParameters(double... params) {
			theta = Math.toRadians(params[0])/2;
		}
		
		public double[] project(double lat, double lon) {
			// TODO: Implement this
			return null;
		}
		
		public double[] inverse(double x, double y) {
			// TODO: Implement this
			return null;
		}
	};
}
