package game;

import engine.Entity;

/**
 * Just makes sure we're drawn at the correct depth... for now
 */
public class GuiEntity extends Entity {

	public GuiEntity(){
		setDrawOrder(20);
	}
	
}
