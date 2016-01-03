package game;

import java.awt.Color;

import engine.Game;

/**
 * The player's heat/overheat indicator
 */
public class HeatMeter extends Meter {
	
	private FlameThrower ft;
	
	public HeatMeter(){
		x = 10;
		y = Game.HEIGHT - 30;
		width = 150;
		height = 20;
		
		ft = GameDevGame.player.getFlameThrower();
	}
	
	public double getFullness(){
		return ft.getHeatFraction();
	}
	
	public Color getColor(){
		if(ft.isOverheating())
			return Color.red;
		else
			return Color.orange;
	}
	
	public String getText(){
		if(ft.isOverheating())
			return "OVERHEAT";
		else
			return "";
	}
}
