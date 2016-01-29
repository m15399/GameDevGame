package main_menu;

import java.awt.Color;

import utils.Observer;

import engine.Input;

/**
 * Button that can be clicked on. Will notify an observer when clicked. 
 */
public class MenuButton extends Label {

	private boolean clicking;
	private Observer callback;
	
	public MenuButton(String theText, double centerX, double centerY, double w, double h, Observer theCallback){
		super(theText, centerX, centerY, w, h);

		setContents(theText);

		clicking = false;
		
		callback = theCallback;
		
		setOutlined(true);
	}
	
	public boolean isClicked(){
		return clicking;
	}
	
	public void update(double dt){
		boolean wasClicking = clicking;
		
		clicking = false;
		if(isHovered()){
			if(Input.isMouseDown() || Input.isMousePressed())
				clicking = true;
		}
		
		// Mouse was pressed and released
		if(wasClicking && !clicking){
			callback.notify(null);
		}
		
		// Set color based on hover/click status
		int regular = 200;
		int hover = 230;
		
		if(isClicked())
			setColor(Color.white);
		else if(isHovered())
			setColor(new Color(hover, hover, hover));
		else 
			setColor(new Color(regular, regular, regular));
	}
	
}
