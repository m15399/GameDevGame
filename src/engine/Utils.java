package engine;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

/**
 * Useful utility functions
 */
public class Utils {
	
	/**
	 * This method makes it easier to draw images centered with rotation and
	 * scale
	 * 
	 * @param filename
	 *            name of image file
	 * @param x
	 *            CENTER x coord
	 * @param y
	 *            CENTER y coord
	 * @param rot
	 *            rotation in radians
	 */
	public static void drawImage(Graphics2D g, String filename, double x,
			double y, double rot, double scale) {
		Image img = Resources.getImage(filename);
		int xs = img.getWidth(null), ys = img.getHeight(null);

		// rotation stuff
		AffineTransform prevTransform = g.getTransform();
		g.translate(x, y);
		g.rotate(rot);
		g.scale(scale, scale);

		// draw the image
		g.drawImage(img, (int) -xs / 2, (int) -ys / 2, null);

		g.setTransform(prevTransform);
	}

	/**
	 * This method makes it easier to draw a rect with rotation and scale
	 * 
	 * @param x
	 *            CENTER x coord
	 * @param y
	 *            CENTER y coord
	 * @param rot
	 *            rotation in radians
	 */
	public static void fillRect(Graphics2D g, double x, double y, double xs,
			double ys, double rot, double scale) {
		// rotation stuff
		AffineTransform prevTransform = g.getTransform();
		g.translate(x, y);
		g.rotate(rot);
		g.scale(scale, scale);

		// fill the rect
		g.fillRect((int) -xs / 2, (int) -ys / 2, (int) xs, (int) ys);

		g.setTransform(prevTransform);
	}

	/**
	 * @return A random value between min and max
	 */
	public static double randomRange(double min, double max) {
		return Math.random() * (max - min) + min;
	}

	/**
	 * @return A random value >= min and < max (does not include max
	 */
	public static int randomRangeInt(int min, int max) {
		return (int) Math.floor(randomRange(min, max));
	}

	/**
	 * @return The value clamped to the range min to max
	 */
	public static double clamp(double val, double min, double max) {
		return Math.max(Math.min(val, max), min);
	}

	/**
	 * @return True if the rect (x, y, xs, ys) collides with the rect (x2, y2,
	 *         xs2, ys2)
	 */
	public static boolean rectsCollide(double x, double y, double xs,
			double ys, double x2, double y2, double xs2, double ys2) {
		return (x + xs / 2 > x2 - xs2 / 2) && (x - xs / 2 < x2 + xs2 / 2)
				&& (y + ys / 2 > y2 - ys2 / 2) && (y - ys / 2 < y2 + ys2 / 2);
	}

	/**
	 * Linear interpolate between two values.
	 * @param t Value between 0 and 1
	 */
	public static double lerp(double a, double b, double t) {
		return (1 - t) * a + t * b;
	}
	
	/**
	 * Log a fatal error and quit
	 */
	public static void fatal(String msg){
		System.err.println("Fatal error: " + msg);
		Application.quit();
	}
	
	public static void err(String msg){
		System.err.println("Error: " + msg);
	}
	
	/**
	 * Do a mod operation that doesn't return negative values
	 */
	public static double mod(double a, double b){
		return (a % b + b) % b;
	}
	
	public static void drawStringCentered(Graphics2D g, String s, int x, int y){
		FontMetrics fm = g.getFontMetrics();
		int w = fm.stringWidth(s);
		int h = fm.getHeight();
		g.drawString(s, x - w/2, y + h/3);
	}
}
