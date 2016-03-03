package utils;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import engine.Resources;

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

//	/**
//	 * @return True if the rect (x, y, xs, ys) collides with the rect (x2, y2,
//	 *         xs2, ys2). (x, y) is the top left
//	 */
//	public static boolean rectsCollide(double x, double y, double xs,
//			double ys, double x2, double y2, double xs2, double ys2) {
//		return (x + xs > x2) && (x < x2 + xs2)
//				&& (y + ys > y2) && (y < y2 + ys2);
//	}
	
	/**
	 * @return whether the point is inside the rect defined by rx, ry, rw, rh.
	 * (rx, ry) is the top left corner. 
	 */
	public static boolean pointInRect(double x, double y, double rx, double ry, 
			double rw,double rh){
		return (x >= rx && y >= ry && x <= rx + rw && y <= ry + rh);
	}

	/**
	 * Linear interpolate between two values.
	 * @param t Value between 0 and 1
	 */
	public static double lerp(double a, double b, double t) {
		t = Math.min(t, 1);
		return (1 - t) * a + t * b;
	}
	
	// Utils.log, Utils.err, and Utils.fatal will pass their string arg
	// to these observers when used
	public static Observer logObserver = null;
	public static Observer errObserver = null;
	public static Observer fatalObserver = null;
	
	public static void log(String msg){
		System.out.println(msg);
		if(logObserver != null)
			logObserver.notify(msg);
	}
	
	/**
	 * Log a fatal error and quit
	 */
	public static void fatal(String msg){
		String errMsg = "Fatal error: " + msg;
		System.err.println(errMsg);
		if(errObserver != null)
			errObserver.notify(errMsg);
		
		if(fatalObserver != null){
			fatalObserver.notify(null);
		} else {
			System.exit(1);
		}
	}
	
	public static void err(String msg){
		String errMsg = "Error: " + msg;
		System.err.println(errMsg);
		if(errObserver != null)
			errObserver.notify(errMsg);
	}
	
	/**
	 * Do a mod operation that doesn't return negative values
	 */
	public static double mod(double a, double b){
		return (a % b + b) % b;
	}
	
	/**
	 * Draw the string centered, instead of left justified
	 */
	public static void drawStringCentered(Graphics2D g, String s, int x, int y){
		FontMetrics fm = g.getFontMetrics();
		int w = fm.stringWidth(s);
		int h = fm.getHeight();
		g.drawString(s, x - w/2, y + h/3);
	}
	
	/**
	 * Set the volume of the audio clip
	 */
	public static void setClipVolume(Clip c, double volume){
		FloatControl vol = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
		vol.setValue((float)volume);
	}
	
	/**
	 * Calculate the size of the object after being serialized
	 */
	public static int calcNetworkSize(Serializable o) {
		try {
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					byteOutputStream);

			objectOutputStream.writeObject(o);

			objectOutputStream.flush();
			objectOutputStream.close();

			return byteOutputStream.toByteArray().length;
		} catch (IOException e) {
			Utils.err("Couldn't calculate object size");
			return 0;
		}
	}
	
	/**
	 * Pack a double into a byte. This is useful for sending doubles over the network when 
	 * you don't need 64 bits of precision. You must supply a min and max value of the double,
	 * so that it can be packed into a smaller space. 
	 * @return A byte representing the double's location between min and max, packed in the 
	 * lower 'nBits' bits of the byte. 
	 */
	public static byte packRange(double val, double min, double max, int nBits){
		if(val < min || val > max){
			Utils.err("Couldn't pack byte, value was outside of range! Fix immediately");
			return 0;
		}
		
		double ratio = (val - min)/(max - min);
		int maxBitVal = (int)Math.pow(2, nBits)-1;
		byte ret = (byte)Math.round(ratio * maxBitVal);
		
		return ret;
	}
	
	/**
	 * Unpack a double that has been packed using 'packRange'. Note that precision is
	 * lost when packing and unpacking, so if you packed 0, for example, don't expect 
	 * this method to return exactly 0 when unpacked. 
	 */
	public static double unpackRange(byte val, double min, double max, int nBits){
		int maxBitVal = (int)Math.pow(2, nBits)-1;
		int vali = val & 0xFF;
		double ratio = (double)vali/maxBitVal;
		double ret = ratio * (max-min) + min;
		return ret;
	}
	
	/**
	 * Pack and unpack a double, then return it. Lets you see how much precision
	 * is lost when packing/unpacking the value. 
	 */
	public static double testCompression(double val, double min, double max, int nBits){
		return unpackRange(packRange(val, min, max, nBits), min, max, nBits);
	}
	
	/**
	 * Smallest difference between 2 angles (in degrees). 
	 */
	public static double angleDifference(double a, double b){
		return Utils.mod((b-a) + 180, 360) - 180;
	}
	

	/**
	 * @return the text in the clipboard, or null if couldn't get it. 
	 */
	public static String readClipboard(){
		String str = null;
		
		try {
			str = (String) Toolkit.getDefaultToolkit()
			        .getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (HeadlessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return str;
	}
}
