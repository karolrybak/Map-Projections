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

import maps.Projection.Property;
import utils.Dixon;

/**
 * Projections created by projecting onto and then unfolding a regular tetrahedron
 * 
 * @author jkunimune
 */
public class Tetrahedral {
	
	public static final Projection LEE =
			new TetrahedralProjection("Lee", 0b1001, Property.CONFORMAL,
					null, "that really deserves more attention") {
		
		public double[] innerProject(double lat, double lon) {
			final de.jtem.mfc.field.Complex z = de.jtem.mfc.field.Complex.fromPolar(
					Math.pow(2, 5/6.)*Math.tan(Math.PI/4-lat/2), lon);
			final de.jtem.mfc.field.Complex w = Dixon.invFunc(z);
			return new double[] { w.abs()*1.132, w.arg() }; //I don't understand Dixon functions well enough to say whence the 1.132 comes
		}
		
		public double[] innerInverse(double r, double tht) {
			final de.jtem.mfc.field.Complex w = de.jtem.mfc.field.Complex.fromPolar(
					r/1.132, tht - Math.PI/2);
			final de.jtem.mfc.field.Complex ans = Dixon.leeFunc(w).times(Math.pow(2, -5/6.));
			return new double[] {
					Math.PI/2 - 2*Math.atan(ans.abs()),
					Math.PI/2 + ans.arg() };
		}
	};
	
	
	public static final Projection TETRAGRAPH =
			new TetrahedralProjection("TetraGraph", 0b1111, Property.EQUIDISTANT,
					null, "that I invented") {
		
		public double[] innerProject(double lat, double lon) {
			final double tht = lon - Math.floor(lon/(2*Math.PI/3))*(2*Math.PI/3) - Math.PI/3;
			return new double[] {
					Math.atan(1/Math.tan(lat)*Math.cos(tht))/Math.cos(tht) /Math.atan(Math.sqrt(2)),
					lon };
		}
		
		public double[] innerInverse(double r, double tht) {
			final double t0 = Math.floor((tht+Math.PI/2)/(2*Math.PI/3)+0.5)*(2*Math.PI/3) - Math.PI/2;
			final double dt = tht-t0;
			return new double[] {
					Math.PI/2 - Math.atan(Math.tan(r*Math.cos(dt)*Math.atan(Math.sqrt(2)))/Math.cos(dt)),
					tht };
		}
	};
	
	
	public static final Projection ACTAAUTHAGRAPH =
			new TetrahedralProjection(
					"Equahedral", 0b1011, Property.EQUAL_AREA, null, "to put AuthaGraph to shame.") {
		
		public double[] innerProject(double lat, double lon) {
			final double Lam = Math.floor(lon/(2*Math.PI/3))*(2*Math.PI/3) + Math.PI/3;
			final double lam = lon - Lam;
			final double tht = Math.atan((lam - Math.asin(Math.sin(lam)/Math.sqrt(3)))/Math.PI*Math.sqrt(108));
			return new double[] {
					Math.sqrt((1 - Math.sin(lat))/(1 - 1/Math.sqrt(1+2/Math.cos(lam))))/Math.cos(tht),
//					(Math.PI/2-lat)/Math.atan(Math.sqrt(2)/Math.cos(lam))/Math.cos(tht),
					Lam + tht };
		}
		
		public double[] innerInverse(double x, double y) {
			// TODO: Implement this
			return null;
		}
		
	};
	
	
	public static final Projection TETRAPOWER =
			new TetrahedralProjection(
					"TetraPower", "A parameterised tetrahedral projection that I invented.",
					0b1111, Property.COMPROMISE, new String[] {"k1","k2","k3"},
					new double[][] {{.01,2.,.98},{.01,2.,1.2},{.01,2.,.98}}) {
		
		private double k1, k2, k3;
		
		public void setParameters(double... params) {
			this.k1 = params[0];
			this.k2 = params[1];
			this.k3 = params[2];
		}
		
		public double[] innerProject(double lat, double lon) {
			final double t0 = Math.floor(lon/(2*Math.PI/3))*(2*Math.PI/3) + Math.PI/3;
			final double tht = lon - t0;
			final double thtP = Math.PI/3*(1 - Math.pow(1-Math.abs(tht)/(Math.PI/2),k1))/(1 - 1/Math.pow(3,k1))*Math.signum(tht);
			final double kRad = k3*Math.abs(thtP)/(Math.PI/3) + k2*(1-Math.abs(thtP)/(Math.PI/3));
			final double rmax = 0.5/Math.cos(thtP); //the max normalized radius of this triangle (in the plane)
			final double rtgf = Math.atan(1/Math.tan(lat)*Math.cos(tht))/Math.atan(Math.sqrt(2))*rmax;
			return new double[] {
					(1 - Math.pow(1-rtgf,kRad))/(1 - Math.pow(1-rmax,kRad))*rmax*2,
					thtP + t0 };
		}
		
		public double[] innerInverse(double r, double tht) {
			final double t0 = Math.floor((tht+Math.PI/2)/(2*Math.PI/3)+0.5)*(2*Math.PI/3) - Math.PI/2;
			final double thtP = tht-t0;
			final double lamS = (1-Math.pow(1-Math.abs(thtP)*(1-1/Math.pow(3,k1))/(Math.PI/3), 1/k1))*Math.PI/2*Math.signum(thtP);
			final double kRad = k3*Math.abs(thtP)/(Math.PI/3) + k2*(1-Math.abs(thtP)/(Math.PI/3));
			final double rmax = 0.5/Math.cos(thtP); //the max normalized radius of this triangle (in the plane)
			final double rtgf = 1-Math.pow(1-r/2/rmax*(1-Math.pow(Math.abs(1-rmax), kRad)), 1/kRad); //normalized tetragraph radius
			return new double[] {
					Math.atan(Math.cos(lamS)/Math.tan(rtgf/rmax*Math.atan(Math.sqrt(2)))),
					t0 + lamS };
		}
	};
	
	
	public static final Projection TETRAFILLET =
			new TetrahedralProjection("TetraFillet",
					"A parameterised tetrahedral projection I invented with the corners filleted off.",
					0b1110, Property.COMPROMISE, new String[] {"k1","k2","k3"},
					new double[][] {{.01,2.,.78},{.01,2.,.99},{.01,2.,1.3}}) {
		
		private double k1, k2, k3;
		
		public void setParameters(double... params) {
			this.k1 = params[0];
			this.k2 = params[1];
			this.k3 = params[2];
		}
		
		public double[] innerProject(double lat, double lon) {
			final double t0 = Math.floor(lon/(2*Math.PI/3))*(2*Math.PI/3) + Math.PI/3;
			final double tht = lon - t0;
			final double thtP = Math.PI/3*(1 - Math.pow(1-Math.abs(tht)/(Math.PI/2),k1))/(1 - 1/Math.pow(3,k1))*Math.signum(tht);
			final double kRad = k3*Math.abs(thtP)/(Math.PI/3) + k2*(1-Math.abs(thtP)/(Math.PI/3));
			final double rmax; //the max normalized radius of this triangle (in the plane)
			if (Math.abs(thtP) < .70123892) 	rmax = .5/Math.cos(thtP);
			else 	rmax = .75 - 1.5972774*Math.pow(Math.PI/3-Math.abs(thtP),2)/2;
			final double rtgf = Math.atan(1/Math.tan(lat)*Math.cos(tht))/Math.atan(Math.sqrt(2))*rmax; //normalized tetragraph radius
			return new double[] {
					(1 - Math.pow(1-rtgf,kRad))/(1 - Math.pow(1-rmax,kRad))*rmax*2,
					thtP + t0
			};
		}
		
		public double[] innerInverse(double r, double tht) {
			final double t0 = Math.floor((tht+Math.PI/2)/(2*Math.PI/3)+0.5)*(2*Math.PI/3) - Math.PI/2;
			final double thtP = tht-t0;
			final double lamS = (1-Math.pow(1-Math.abs(thtP)*(1-1/Math.pow(3,k1))/(Math.PI/3), 1/k1))*Math.PI/2*Math.signum(thtP);
			final double kRad = k3*Math.abs(thtP)/(Math.PI/3) + k2*(1-Math.abs(thtP)/(Math.PI/3));
			final double rmax; //the max normalized radius of this triangle (in the plane)
			if (Math.abs(thtP) < .70123892) 	rmax = .5/Math.cos(thtP);
			else 	rmax = .75 - 1.5972774*Math.pow(Math.PI/3-Math.abs(thtP),2)/2;
			final double rtgf = 1-Math.pow(1-r/2/rmax*(1-Math.pow(Math.abs(1-rmax), kRad)), 1/kRad); //normalized tetragraph radius
			if (r/2 > rmax) 	return null;
			return new double[] {
					Math.atan(Math.cos(lamS)/Math.tan(rtgf/rmax*Math.atan(Math.sqrt(2)))),
					t0 + lamS };
		}
	};
	
	
	public static final Projection TETRACHAMFER =
			new TetrahedralProjection(
					"TetraChamfer",
					"A parameterised tetrahedral projection I invented with the corners chamfered off.",
					0b1110, Property.COMPROMISE, new String[] {"k1","k2","k3"},
					new double[][] {{.01,2.,.78},{.01,2.,.99},{.01,2.,1.3}}) {
		
		private double k1, k2, k3;
		
		public void setParameters(double... params) {
			this.k1 = params[0];
			this.k2 = params[1];
			this.k3 = params[2];
		}
		
		public double[] innerProject(double lat, double lon) {
			final double t0 = Math.floor(lon/(2*Math.PI/3))*(2*Math.PI/3) + Math.PI/3;
			final double tht = lon - t0;
			final double thtP = Math.PI/3*(1 - Math.pow(1-Math.abs(tht)/(Math.PI/2),k1))/(1 - 1/Math.pow(3,k1))*Math.signum(tht);
			final double kRad = k3*Math.abs(thtP)/(Math.PI/3) + k2*(1-Math.abs(thtP)/(Math.PI/3));
			final double rmax = Math.min(.5/Math.cos(thtP), .75/Math.cos(Math.PI/3-Math.abs(thtP))); //the max normalized radius of this triangle (in the plane)
			final double rtgf = Math.atan(1/Math.tan(lat)*Math.cos(tht))/Math.atan(Math.sqrt(2))*rmax; //normalized tetragraph radius
			return new double[] {
					(1 - Math.pow(1-rtgf,kRad))/(1 - Math.pow(1-rmax,kRad))*rmax*2,
					thtP + t0 };
		}
		
		public double[] innerInverse(double r, double tht) {
			final double t0 = Math.floor((tht+Math.PI/2)/(2*Math.PI/3)+0.5)*(2*Math.PI/3) - Math.PI/2;
			final double thtP = tht-t0;
			final double lamS = (1-Math.pow(1-Math.abs(thtP)*(1-1/Math.pow(3,k1))/(Math.PI/3), 1/k1))*Math.PI/2*Math.signum(thtP);
			final double kRad = k3*Math.abs(thtP)/(Math.PI/3) + k2*(1-Math.abs(thtP)/(Math.PI/3));
			final double rmax = Math.min(.5/Math.cos(thtP), .75/Math.cos(Math.PI/3-Math.abs(thtP))); //the max normalized radius of this triangle (in the plane)
			final double rtgf = 1-Math.pow(1-r/2/rmax*(1-Math.pow(Math.abs(1-rmax), kRad)), 1/kRad); //normalized tetragraph radius
			if (r/2 > rmax) 	return null;
			return new double[] {
					Math.atan(Math.cos(lamS)/Math.tan(rtgf/rmax*Math.atan(Math.sqrt(2)))),
					t0 + lamS };
		}
	};
	
	
	
	/**
	 * A base for tetrahedral projections
	 * 
	 * @author jkunimune
	 */
	private static abstract class TetrahedralProjection extends Projection {
		
		private static final double PHI_0 = Math.asin(1/3.); //the complement of the angular radius of a face
		
		public TetrahedralProjection(
				String name, int fisc, Property property, String adjective, String addendum) {
			super(name, 6, 2*Math.sqrt(3), fisc, Type.TETRAHEDRAL, property, adjective, addendum);
		}
		
		public TetrahedralProjection(
				String name, String description, int fisc, Property property) {
			super(name, description, 6, 2*Math.sqrt(3), fisc, Type.TETRAHEDRAL, property);
		}
		
		public TetrahedralProjection(
				String name, String description, int fisc, Property property,
				String[] paramNames, double[][] paramValues) {
			super(name, description, 6, 2*Math.sqrt(3), fisc, Type.TETRAHEDRAL, property, paramNames,
					paramValues);
		}
		
		
		protected abstract double[] innerProject(double lat, double lon); //the projection from spherical to polar within a face
		
		protected abstract double[] innerInverse(double x, double y); //I think you can guess
		
		
		public double[] project(double lat, double lon) {
			final double[][] centrums = {
					{-Math.PI/2,	 0,			 Math.PI/3,	 Math.PI/6},
					{ PHI_0,		 Math.PI,	 Math.PI/3,	 Math.PI/6},
					{ PHI_0,		 Math.PI/3,	 Math.PI/3,	 0},
					{ PHI_0,		-Math.PI/3,	-Math.PI/3,	 0}};
			double latR = Double.NaN;
			double lonR = Double.NaN;
			byte poleIdx = -1;
			for (byte i = 0; i < 4; i++) {
				final double[] relCoords = obliquifySphc(lat, lon, centrums[i]);
				if (Double.isNaN(latR) || relCoords[0] > latR) {
					latR = relCoords[0]; // pick the centrum that maxes out your latitude
					lonR = relCoords[1];
					poleIdx = i;
				}
			}
			
			final double[] rth = innerProject(latR, lonR);
			final double r = rth[0];
			final double th = rth[1] - centrums[poleIdx][3];
			
			switch (poleIdx) {
			case 0:
				if (Math.sin(lon) < 0)
					return new double[]
							{-2+r*Math.sin(th), -Math.sqrt(3)-r*Math.cos(th)}; // lower left
				else
					return new double[]
							{ 2-r*Math.sin(th), -Math.sqrt(3)+r*Math.cos(th)}; // lower right
			case 1:
				if (Math.sin(lon) < 0)
					return new double[]
							{-2+r*Math.sin(th),  Math.sqrt(3)-r*Math.cos(th)}; // lower left
				else
					return new double[]
							{ 2-r*Math.sin(th),  Math.sqrt(3)+r*Math.cos(th)}; // lower right
			case 2:
				return new double[]
						{ 1 + r*Math.cos(th),  r*Math.sin(th)}; // right
			case 3:
				return new double[]
						{-1 - r*Math.cos(th), -r*Math.sin(th)}; // left
			default:
				return null;
			}
		}
		
		
		public double[] inverse(double x, double y) {
			final double[] faceCenter;
			final double dt, xp, yp;
			if (3 + Math.sqrt(3)*y < x) { //lower right
				faceCenter = new double[] { -Math.PI/2, 0, Math.PI/2 };
				dt = -Math.PI/2;
				xp = x - 2;
				yp = y + Math.sqrt(3);
			}
			else if (3 + Math.sqrt(3)*y < -x) { //lower left
				faceCenter = new double[] { -Math.PI/2, 0, Math.PI/2 };
				dt = Math.PI/2;
				xp = x + 2;
				yp = y + Math.sqrt(3);
			}
			else if (3 - Math.sqrt(3)*y < x) { //upper right
				faceCenter = new double[] { PHI_0, Math.PI, Math.PI/2 };
				dt = -Math.PI/2;
				xp = x - 2;
				yp = y - Math.sqrt(3);
			}
			else if (3 - Math.sqrt(3)*y < -x) { //upper left
				faceCenter = new double[] { PHI_0, Math.PI, Math.PI/2 };
				dt = Math.PI/2;
				xp = x + 2;
				yp = y - Math.sqrt(3);
			}
			else if (x < 0) { //left
				faceCenter = new double[] { PHI_0, -Math.PI/3, Math.PI/2 };
				dt = Math.PI/6;
				xp = x + 1;
				yp = y;
			}
			else { //right
				faceCenter = new double[] { PHI_0, Math.PI/3, Math.PI/2 };
				dt = -Math.PI/6;
				xp = x - 1;
				yp = y;
			}
			
			return obliquifyPlnr(
					innerInverse(Math.hypot(xp, yp), Math.atan2(yp, xp)+dt), faceCenter);
		}
	}
}
