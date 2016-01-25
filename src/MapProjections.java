import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import ellipticFunctions.Jacobi;
import mfc.field.Complex;

/**
 * 
 */

/**
 * @author Justin Kunimune
 *
 */
public class MapProjections implements ActionListener {
	private static final String[] PROJ = {"Equirectangular","Mercator","Gall","Cylindrical Equal-Area",
			"Polar","Stereographic","Azimuthal Equal-Area","Orthogonal","Gnomic","Lambert Conical",
			"Pierce-Quincuncial","Sinusoidal","Lemons","Shifted Quincuncial" };
	private static final String[] TIP3 = {"A basic cylindrical map",
											"A shape-preserving cylindrical map",
											"A compromising cylindrical map",
											"An area-preserving cylindrical map",
											"A basic azimuthal map",
											"A shape-preserving azimuthal map",
											"An area-preserving azimuthal map",
											"Represents earth viewed from an infinite distance",
											"Every straight line on the map is a straight line on the sphere",
											"A conical map (conical maps suck; don't use this one)",
											"A conformal square map that uses complex math",
											"An area-preserving map shaped like a sinusoid",
											"BURN LIFE'S HOUSE DOWN!",
											"A reorganized version of Pierce Quincuncial and actually the best map ever" };
	
	private static final String[] FILE = {"Satellite","Altitude","Blackandwhite","HiContrast","Terrain",
			"NoIce","Biomes","Political","Timezones","Historic","Population","Antipode","Empire","Mars",
			"Stars","ColorWheel","Grid","Soccer"};
	private static final String[] TIP1 = {"A realistic rendering of the Earth",
											"Color-coded based on altitude",
											"Land is black; oceans are white.",
											"Lots of snow and ice; oceans are black.",
											"Color-coded based on terrain",
											"Color-coded based on terrain, without ice",
											"Color-coded based on biome",
											"Political map with country names removed",
											"A map of different time-zones",
											"An old map by European explorers",
											"Color-coded by population",
											"If you dug straight down, where would you end up?",
											"The British Empire at its height",
											"A realistic rendering of Mars",
											"The cosmos, as seen from Earth",
											"Color-coded by latitude and longitude",
											"Each square represents 30 degrees.",
											"A realistic rendering of a football" };
	
	private static final String AXES = "cstmjnlx123";
	private static final String[] AXIS_NAMES = {"Standard","Transverse","Center of Mass","Jerusalem",
			"Point Nemo","Longest Line","Longest Line Transverse","Cylindrical","Conical","Quincuncial"};
	private static final String[] TIP2 = {"The north pole (standard for most maps)",
											"Offset from standard by 90 degrees",
											"The center of landmass on Earth (Giza)",
											"The city of Jerusalem",
											"Antipode of the point farthest from land",
											"Sets the longest sailable line as the equator",
											"Sets the longest sailable line as the meridian",
											"Perfect for cylindrical maps",
											"Perfect for conical maps",
											"Perfect for the Pierce Quincuncial projection" };
	private static final double[] lats = {90,0,29.9792,31.7833,48.8767,-28.5217,-46.4883,-35,10,59};
	private static final double[] lons = {0,0,31.1344,35.216,56.6067,141.451,16.5305,-13.6064,-115,19};
	private static final double[] thts = {0,180,-32,-35,-45,71.5,137,145,150,50};
	
	
	public String command = "";
	
	
	
	
	public static void main(String[] args) {
		BufferedImage input, output;
		int w;
		double x2y;
		double latD, lonD, thtD;
		String projection;
		
		MapProjections listener = new MapProjections(); // initialization
		JFrame frame = new JFrame("Map Configurer");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setSize(400,300);
	    
	    while (true) { // make as many maps as you want
	
			JPanel panel = new JPanel();
			JLabel label = new JLabel("Please select a map theme."); // select map theme
			panel.add(label);
			JButton buttn;
			for (int i = 0; i < FILE.length; i ++) {
			    buttn = new JButton(FILE[i]);
			    buttn.setToolTipText(TIP1[i]);
			    buttn.setActionCommand(FILE[i]);
			    buttn.addActionListener(listener);
			    panel.add(buttn);
			}
		    frame.add(panel);
		    frame.setVisible(true);
		    
		    while (listener.isWaiting()) {} // waits for a button to be pressed
		    try {
		    	input = ImageIO.read(new File("input/"+listener.command+".jpg"));
		    } catch (IOException e) {
		    	System.err.println("Where the heck is the image?!");
		    	return;
		    }
		    listener.reset();
		    frame.remove(panel);
		    
		    panel = new JPanel();
			label = new JLabel("Pick an aspect ratio and pixel-width."); // select map dimensions
			panel.add(label);
			SpinnerModel ratioModel = new SpinnerNumberModel(1, .1, 10, .01);
			JSpinner ratio = new JSpinner(ratioModel);
			SpinnerModel widthModel = new SpinnerNumberModel(800, 400, 10000, 1);
			JSpinner width = new JSpinner(widthModel);
			panel.add(ratio);
			panel.add(width);
		    buttn = new JButton("OK");
		    buttn.setToolTipText("Press when you are satisfied with your dimensions.");
		    buttn.setActionCommand("OK");
		    buttn.addActionListener(listener);
		    panel.add(buttn);
		    frame.add(panel);
		    frame.setVisible(true);
		    
		    while (listener.isWaiting()) {} // wait for a button to be pressed
		    w = (int)(width.getValue());
		    x2y = (double)(ratio.getValue());
			output = new BufferedImage(w,(int)(w/x2y),BufferedImage.TYPE_INT_RGB);
		    listener.reset();
		    frame.remove(panel);
			
		    panel = new JPanel();
			label = new JLabel("Specify an axis (latitude, longitude, orientation),\n or choose a preset."); // select axis
			panel.add(label);
			SpinnerModel latModel = new SpinnerNumberModel(90, -90, 90, .1);
			JSpinner lat = new JSpinner(latModel);
			SpinnerModel lonModel = new SpinnerNumberModel(0, -180, 180, .1);
			JSpinner lon = new JSpinner(lonModel);
			SpinnerModel thtModel = new SpinnerNumberModel(0, -180, 180, .1);
			JSpinner tht = new JSpinner(thtModel);
			panel.add(lat);
			panel.add(lon);
			panel.add(tht);
		    buttn = new JButton("Use Custom");
		    buttn.setToolTipText("Create a custom axis from the specified coordinates.");
		    buttn.setActionCommand("c");
		    buttn.addActionListener(listener);
		    panel.add(buttn);
		    for (int i = 0; i < AXIS_NAMES.length; i ++) {
		    	buttn = new JButton(AXIS_NAMES[i]);
		    	buttn.setToolTipText(TIP2[i]);
		    	buttn.setActionCommand(String.valueOf(AXES.charAt(i+1)));
		    	buttn.addActionListener(listener);
		    	panel.add(buttn);
		    }
		    frame.add(panel);
		    frame.setVisible(true);
		    
		    while (listener.isWaiting()) {} // wait for a button to be pressed
		    int n = AXES.indexOf(listener.command);
			if (n > 0) { // if it is a preset
				latD = lats[n-1];
				lonD = lons[n-1];
				thtD = thts[n-1];
			}
			else { // if it is custom
				latD = (double)lat.getValue();
				lonD = (double)lon.getValue();
				thtD = (double)tht.getValue();
			}
		    listener.reset();
		    frame.remove(panel);
			
		    panel = new JPanel();
			label = new JLabel("Finally, pick a projection."); // select projection
			panel.add(label);
			for (int i = 0; i < PROJ.length; i ++) {
			    buttn = new JButton(PROJ[i]);
			    buttn.setToolTipText(TIP3[i]);
			    buttn.setActionCommand(PROJ[i]);
			    buttn.addActionListener(listener);
			    panel.add(buttn);
			}
		    frame.add(panel);
		    frame.setVisible(true);
		    
		    while (listener.isWaiting()) {} // wait for a button to be pressed
		    projection = listener.command;
		    frame.remove(panel);
		    
		    panel = new JPanel();
			label = new JLabel("Wait..."); // select map dimensions
			panel.add(label);
			frame.add(panel);
			frame.setVisible(true);
			map(input,output,projection,latD,lonD,thtD);
			
			saveImage(output);
			
			frame.remove(panel);
			listener.reset();
			panel = new JPanel();
			label = new JLabel("Done!"); // finished!
			panel.add(label);
			buttn = new JButton("Make Another");
			buttn.setToolTipText("You know you want to..."); // lets you start over
			buttn.setActionCommand("Go!");
			buttn.addActionListener(listener);
			panel.add(buttn);
			frame.add(panel);
			frame.setVisible(true);
			while (listener.isWaiting()) {}
			frame.remove(panel);
			listener.reset();
		}
	}
	
	
	
	/* PROJECTION METHODS: Return RGB at a given pixel based on a reference map and a unique projection method */
	public static int quincuncial(final double lat0, final double lon0, final double orientation,
			                      final int width, final int height, int x, int y, BufferedImage ref) { // a tessalatable square map
		Complex u = new Complex(3.7116*x/width, 3.7116*y/height-1.8558); // don't ask me where 3.7116 come from because I have no idea
		Complex k = new Complex(Math.sqrt(0.5)); // the rest comes from some fancy complex calculus stuff
		Complex ans = Jacobi.cn(u, k);
		double p = 2*Math.atan(ans.abs());
		double theta = Math.atan2(ans.getRe(), ans.getIm());
		double lambda = p-Math.PI/2;
		return getColor(lat0, lon0, orientation, lambda, theta, ref);
	}
	
	
	public static int equirectangular(final double lat0, final double lon0, final double orientation,
			                          final int width, final int height, int x, int y, BufferedImage ref) { // a basic scale
		return getColor(lat0,lon0,orientation, (double)y/height*Math.PI - Math.PI/2, (double)x/width*2*Math.PI, ref);
	}
	
	
	public static int mercator(final double lat0, final double lon0, final double orientation,
		                       final int width, final int height, int x, int y, BufferedImage ref) { // a popular shape-preserving map
		y -= height/2;
		double phi = Math.atan(Math.sinh((double)y/width*2*Math.PI)); // latitude from 0 to pi
		return getColor(lat0,lon0,orientation, phi, (double)x/width*2*Math.PI, ref);
	}
	
	
	public static int polar(final double lat0, final double lon0, final double orientation,
		                       final int width, final int height, int x, int y, BufferedImage ref) { // the projection used on the UN flag
		double phi = 2*Math.PI*Math.hypot((double)x/width-.5, (double)y/height-.5) - Math.PI/2;
		if (Math.abs(phi) < Math.PI/2)
			return getColor(lat0,lon0,orientation, phi, Math.atan2(width/2.0-x, height/2.0-y), ref);
		else
			return 0;
	}
	
	
	public static int gall(final double lat0, final double lon0, final double orientation,
            final int width, final int height, int x, int y, BufferedImage ref) { // a compromise map, similar to mercator
		return getColor(lat0, lon0, orientation,
			      	    2*Math.atan((y-height/2.0) / (height/2.0)), x*2*Math.PI/width, ref);
	}
	
	
	public static int sinusoidal(final double lat0, final double lon0, final double orientation,
            final int width, final int height, int x, int y, BufferedImage ref) { // a map shaped like a sinusoid
		return getColor(lat0, lon0, orientation, y*Math.PI/height - Math.PI/2,
				        Math.PI * (x-width/2.0) / (Math.sin(Math.PI*y/height)*width/2.0)+Math.PI, ref);
	}
	
	
	public static int stereographic(final double lat0, final double lon0, final double orientation,
            final int width, final int height, int x, int y, BufferedImage ref) { // a shape-preserving infinite map
		double radius = Math.pow(Math.pow(width, -2)+Math.pow(height, -2), -.5) / Math.PI;
		return getColor(lat0, lon0, orientation, 2*Math.atan(Math.hypot(x-width/2, y-height/2) / radius)-Math.PI/2,
                Math.atan2(width/2.0-x, height/2.0-y), ref);
	}
	
	
	public static int gnomic(final double lat0, final double lon0, final double orientation,
            final int width, final int height, int x, int y, BufferedImage ref) { // a shape-preserving infinite map
		double radius = Math.pow(Math.pow(width, -2)+Math.pow(height, -2), -.5) / Math.PI;
		return getColor(lat0, lon0, orientation, Math.atan(Math.hypot(x-width/2, y-height/2) / radius)-Math.PI/2,
                Math.atan2(width/2.0-x, height/2.0-y), ref);
	}
	
	
	public static int orthogonal(final double lat0, final double lon0, final double orientation,
            final int width, final int height, int x, int y, BufferedImage ref) { // a map that mimics the view from space
		double R = 2*Math.hypot((double)x/width-.5, (double)y/height-.5);
		if (R <= 1)
			return getColor(lat0, lon0, orientation, -Math.acos(R), Math.atan2(x-width/2.0,y-height/2.0), ref);
		else
			return 0;
	}
	
	
	public static int eaCylindrical(final double lat0, final double lon0, final double orientation,
            final int width, final int height, int x, int y, BufferedImage ref) { // an equal area cylindrical map
		return getColor(lat0, lon0, orientation, Math.asin((y-height/2.0) / (height/2.0)),
                        x*2*Math.PI / width, ref);
	}
	
	
	public static int lambert(final double lat0, final double lon0, final double orientation,
            final int width, final int height, int x, int y, BufferedImage ref) { // a conical projection
		double radius = Math.pow(Math.pow(width, -2)+Math.pow(height, -2), -.5) / Math.PI;
		return getColor(lat0, lon0, orientation, 4.0/3.0*(Math.atan(Math.hypot(x-width/2,y)/(radius)-1)+Math.PI/4) - Math.PI/2,
				        2*Math.atan2(width/2.0-x, -y)-Math.PI, ref);
	}
	
	
	public static int lemons(final double lat0, final double lon0, final double orientation,
            final int width, final int height, int x, int y, BufferedImage ref) { // a simple map that is shaped like lemons
		int lemWdt;
		if (width > 12)  lemWdt= width/12; // the pixel width of each lemon
		else             lemWdt = width;
		
		if (Math.abs(x%lemWdt-lemWdt/2.0) < Math.sin(Math.PI*y/height)*lemWdt/2.0) // if it is inside a sin curve
		      return getColor(lat0,lon0,orientation, y*Math.PI/height - Math.PI/2,
		    		          Math.PI * (x%lemWdt-lemWdt/2.0) / (Math.sin(Math.PI*y/height)*lemWdt*6.0) + x/lemWdt*Math.PI/6, ref);
		else
			return 0;
	}
	
	
	public static int eaAzimuth(final double lat0, final double lon0, final double orientation,
            final int width, final int height, int x, int y, BufferedImage ref) { // the lambert azimuthal equal area projection
		double R = 4*Math.hypot((double)x/width-.5, (double)y/height-.5);
		if (R <= 2)
			return getColor(lat0, lon0, orientation, Math.asin(R*R/2-1), Math.atan2(x-width/2.0, y-height/2.0), ref);
		else
			return 0;
	}
	
	
	public static int quinshift(final double lat0, final double lon0, final double orientation,
            final int width, final int height, int x, int y, BufferedImage ref) { // a tessalatable rectangle map
		Complex u = new Complex(3.7116*(0.5*y/height + 1.0*x/width),
				                3.7116*(0.5*y/height - 1.0*x/width)); // don't ask me where 3.7116 come from because I have no idea
		Complex k = new Complex(Math.sqrt(0.5)); // the rest comes from some fancy complex calculus stuff
		Complex ans = Jacobi.cn(u, k);
		double p = 2*Math.atan(ans.abs());
		double theta = Math.atan2(ans.getRe(), ans.getIm());
		double lambda = p-Math.PI/2;
		return getColor(lat0, lon0, orientation, lambda, theta, ref);
	}
	/*END PROJECTION METHODS*/
	
	
	public static int getColor(final double lat0, final double lon0, final double orientation,
			                   double lat1, double lon1, BufferedImage ref) { // returns the color of any coordinate on earth		
		lon1 += orientation;
		double latitude = Math.asin(Math.sin(lat0)*Math.sin(lat1) + Math.cos(lat0)*Math.cos(lon1)*Math.cos(lat1));
		double longitude;
		if (lat0  >= Math.PI/2)
			longitude = lon1+Math.PI;
		else if (lat0 <= -Math.PI/2)
			longitude = -lon1;
		else if (Math.sin(lon1) < 0)
			longitude = lon0 + Math.acos(Math.sin(lat1)/Math.cos(lat0)/Math.cos(latitude)-Math.tan(lat0)*Math.tan(latitude));
		else
			longitude = lon0 - Math.acos(Math.sin(lat1)/Math.cos(lat0)/Math.cos(latitude)-Math.tan(lat0)*Math.tan(latitude));
		
		int x = (int)(longitude*ref.getWidth()/(2*Math.PI));
		int y = (int)((latitude*ref.getHeight()/Math.PI)+ref.getHeight()/2.0);
		
		while (x < 0)
			x += ref.getWidth();
		x %= ref.getWidth();
		if (y < 0)
			y = 0;
		else if (y >= ref.getHeight())
			y = ref.getHeight()-1;
				
		return ref.getRGB(x, y);
	}
	
	
	public static void map(BufferedImage input, BufferedImage output, String projection, double latD, double lonD, double thtD) {
		final int width = output.getWidth();
		final int height = output.getHeight();
		final double lat0 = Math.toRadians(latD);
		final double lon0 = Math.toRadians(lonD);
		final double tht0 = Math.toRadians(thtD+180);
		
		for (int x = 0; x < output.getWidth(); x ++) {
			for (int y = 0; y < output.getHeight(); y ++) {
				switch (projection) {
				case "Pierce-Quincuncial":
					output.setRGB(x, y, quincuncial(lat0,lon0,tht0,width,height,x,y,input));
					break;
				case "Equirectangular":
					output.setRGB(x, y, equirectangular(lat0,lon0,tht0,width,height,x,y,input));
					break;
				case "Mercator":
					output.setRGB(x, y, mercator(lat0,lon0,tht0,width,height,x,y,input));
					break;
				case "Polar":
					output.setRGB(x, y, polar(lat0,lon0,tht0,width,height,x,y,input));
					break;
				case "Gall":
					output.setRGB(x, y, gall(lat0,lon0,tht0,width,height,x,y,input));
					break;
				case "Sinusoidal":
					output.setRGB(x, y, sinusoidal(lat0,lon0,tht0,width,height,x,y,input));
					break;
				case "Stereographic":
					output.setRGB(x, y, stereographic(lat0,lon0,tht0,width,height,x,y,input));
					break;
				case "Orthogonal":
					output.setRGB(x, y, orthogonal(lat0,lon0,tht0,width,height,x,y,input));
					break;
				case "Lemons":
					output.setRGB(x, y, lemons(lat0,lon0,tht0,width,height,x,y,input));
					break;
				case "Azimuthal Equal-Area":
					output.setRGB(x, y, eaAzimuth(lat0,lon0,tht0,width,height,x,y,input));
					break;
				case "Cylindrical Equal-Area":
					output.setRGB(x, y, eaCylindrical(lat0,lon0,tht0,width,height,x,y,input));
					break;
				case "Lambert Conical":
					output.setRGB(x, y, lambert(lat0,lon0,tht0,width,height,x,y,input));
					break;
				case "Gnomic":
					output.setRGB(x, y, gnomic(lat0,lon0,tht0,width,height,x,y,input));
					break;
				case "Shifted Quincuncial":
					output.setRGB(x, y, quinshift(lat0,lon0,tht0,width,height,x,y,input));
					break;
				default:
					System.err.println("Justin, you forgot to add a projection to the switch case! (or you forgot a break;)");
				}
			}
		}
	}
	
	
	private static void saveImage(BufferedImage img) {
		try {
			File outputFile = new File("output/myMap.jpg");
			ImageIO.write(img, "jpg", outputFile);
			Desktop.getDesktop().open(outputFile);
		} catch (IOException e) {}
	}
	
	
	
	
	
	
	public void actionPerformed(ActionEvent e) { // the non-static part of the program acts as a button-listener
		command = e.getActionCommand();
	}
	
	
	public void reset() {
		command = "";
	}
	
	
	public boolean isWaiting() {
		if (command.isEmpty()) {
			System.out.print(""); // this line makes the code work. I've no idea why.
			return true;
		}
		else
			return false;
	}
}