package game;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import engine.GameObject;
import engine.Resources;
import engine.Utils;

/**
 * Repeating background image
 */
public class Background extends GameObject {

	public static final double DEFAULT_DEPTH = 3;
	
	private Image bgImage;
	private int w, h;
	
	// Determines how fast the background moves compared to the ground
	public double depth;
	
	public Background(String imageFile){
		bgImage = Resources.getImage(imageFile);
		w = bgImage.getWidth(null);
		h = bgImage.getHeight(null);
		depth = DEFAULT_DEPTH;
		
		setDrawOrder(-30);
	}
	
	public void draw(Graphics2D g){
		double[] pos = Camera.currCamera.getPos();
		
		AffineTransform prevTransform = g.getTransform();
		
		double tx = Utils.mod(-pos[0]/depth, w);
		double ty = Utils.mod(-pos[1]/depth, h);
		g.translate(tx, ty);
		
		g.drawImage(bgImage, 0, 0, null);
		g.drawImage(bgImage, -w, 0, null);
		g.drawImage(bgImage, 0, -h, null);
		g.drawImage(bgImage, -w, -h, null);
		
		g.setTransform(prevTransform);
	}
	
}
