package game;

import java.awt.Color;

import engine.Game;

/**
 * The player's heat/overheat indicator
 */
public class HeatMeter extends Meter {
		
	public HeatMeter(){
		x = 10;
		y = Game.HEIGHT - 30;
	}
	
	private FlameThrower findFt(){
		Player p = Globals.player;
		if(p != null)
			return p.getFlameThrower();
		return null;
	}
	
	public double getFullness(){
		FlameThrower ft = findFt();
		if(ft != null)
			return ft.getHeatFraction();
		else
			return 0;
	}
	
	public Color getColor(){
		FlameThrower ft = findFt();
		
		if(ft != null && ft.isOverheating())
			return Color.red;
		else
			return Color.orange;
	}
	
	public String getText(){
		FlameThrower ft = findFt();
		if(ft != null && ft.isOverheating())
			return "OVERHEAT";
		else
			return "";
	}
}
