package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import utils.Utils;

import engine.Entity;
import engine.Input;

/** 
 * Displays text centered around a point, inside the bounds of a rect. 
 */
public class Label extends Entity {

	private String contents;
	private double width, height;
	private Color color;
	private boolean isOutlined;
	
	public Label(String theContents, double centerX, double centerY, double w, double h){
		contents = theContents;
		
		// (x, y) is the top left
		x = centerX - w/2;
		y = centerY - h/2;
		width = w;
		height = h;
		
		color = Color.white;
		isOutlined = false;
	}
	
	public void setOutlined(boolean b){
		isOutlined = b;
	}
	
	public void setColor(Color c){
		color = c;
	}
	
	public void setContents(String s){
		contents = s;
	}
	
	public String getContents(){
		return contents;
	}
	
	/**
	 * Is the mouse currently hovering over us?
	 */
	public boolean isHovered(){
		double mouseX = Input.getMouseLoc().x;
		double mouseY = Input.getMouseLoc().y;
		if(Utils.pointInRect(mouseX, mouseY, x, y, width, height))
			return true;
		else
			return false;
	}
	
	/** 
	 * Draws the text, and the outline if isOutlined == true.
	 */
	public void draw(Graphics2D g){
		g.setFont(new Font("Arial", Font.PLAIN, (int) (height * .5)));

		g.setColor(color);

		// Shorten the contents to fit inside the rect
		String contentsShortened = contents;
		FontMetrics fm = g.getFontMetrics();
		while(fm.stringWidth(contentsShortened) > width){
			contentsShortened = contentsShortened.substring(1);
		}
		
		Utils.drawStringCentered(g, contentsShortened, (int)(x + width/2), (int)(y + height/2));
		
		if(isOutlined)
			g.drawRect((int)x, (int)y, (int)width, (int)height);
	}
	
}
