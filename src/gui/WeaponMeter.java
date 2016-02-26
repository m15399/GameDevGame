package gui;

import java.awt.Color;


import engine.Game;
import game.Globals;
import game.Player;
import game.weapon.Weapon;

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
		return new Color(100, 200, 255);
	}
	
	public String getText(){
		// Show the weapon's name
		Weapon w = findWeapon();
		if(w != null)
			return w.getName();
		else
			return "";
	}
}
