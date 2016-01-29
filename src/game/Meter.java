package game;

import java.awt.Color;
import java.awt.Graphics2D;

import utils.Utils;


/**
 * Generic bar meter for tracking a stat
 */
public abstract class Meter extends GuiEntity {

	public int width = 150;
	public int height = 20;

	/**
	 * Override this - return a number between 0 and 1 indicating how full 
	 * the meter should be.
	 */
	public abstract double getFullness();
	
	/**
	 * Override this - return the color of the meter
	 */
	public Color getColor(){
		return Color.gray;
	}
	
	/**
	 * Override this - return the text inside the meter
	 */
	public String getText(){
		return "";
	}
	
	public void draw(Graphics2D g){
		g.setColor(getColor());
		g.fillRect((int)x, (int)y, (int)(width * getFullness()), height);
		
		g.setColor(Color.white);
		g.drawRect((int)x, (int)y, width, height);
		
		Utils.drawStringCentered(g, getText(), (int)x + width/2, (int)y + height/2);
	}
}
