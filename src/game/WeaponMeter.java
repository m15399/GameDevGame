package game;

import java.awt.Color;

import engine.Game;

public class WeaponMeter extends Meter {
	
	public WeaponMeter(){
		x = 10 + width + 10;
		y = Game.HEIGHT - 30;
	}
	
	private Weapon findWeapon(){
		Player p = Globals.player;
		if(p != null)
			return p.getCurrWeapon();
		return null;
	}
	
	public double getFullness(){
		Weapon w = findWeapon();
		if(w != null)
			return w.getChargePct();
		else
			return 0;
	}
	
	public Color getColor(){
		return Color.cyan;
	}
	
	public String getText(){
		// Show the weapon's name
		Weapon w = findWeapon();
		return w.getName();
	}
}
