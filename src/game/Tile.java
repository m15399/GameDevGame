package game;

import java.awt.Color;
import java.awt.Graphics2D;

import engine.Entity;
import engine.Game;
import engine.Resources;
import engine.Utils;

/**
 * Represents a single Tile on the map. Holds its Tile type and determines how
 * to draw it.
 */
public class Tile extends Entity {

	public static final int SIZE = 64;
	
	// How fast tile cools down
	private static final double COOLDOWN_RATE = 1;
	
	// How much extra heat is required to bump up/down to the next stage of heat.
	private static final double TOLERANCE = .5;

	// Levels of heat
	private static final double BURN_HEAT = 10;
	private static final double HEAT_CAP = 20;
	
	/**
	 * Type of a tile
	 */
	public enum Type {
		EMPTY, FLOOR, WALL
	}
	
	public Type type;
	
	public int xc, yc;

	private double heat;
	private boolean burning;
	private Fire fire;

	/**
	 * Decide how to create a tile based on a given token string
	 */
	public Tile(String token, int xc, int yc) {
		this.type = Type.EMPTY;
		burning = false;
		fire = null;
		heat = 0;

		this.xc = xc;
		this.yc = yc;
		x = xc * SIZE;
		y = yc * SIZE;
		
		char c = token.charAt(0);
		switch (c) {

		case 'f':
			catchFire();
		case '1':
			type = Type.FLOOR;
			break;
		case '2':
			type = Type.WALL;
			new WallVisual(xc, yc);
			break;
		}
	}

	private void catchFire() {
		if(!burning && type == Type.FLOOR){
			burning = true;
			fire = new Fire(x + SIZE/2, y + SIZE/2);
		}
	}
	
	private void putOutFire(){
		if(burning && type == Type.FLOOR){
			fire.destroy();
			fire = null;
			burning = false;	
		}	
	}
	
	public void addHeat(double amt){
		if(amt < 0){
			Utils.err("Amount must be positive");
			return;
		}
		heat += amt;
		heat = Math.min(HEAT_CAP, heat);

		if(!burning && heat > BURN_HEAT + TOLERANCE){
			catchFire();
		}
	}
	
	public void subtractHeat(double amt){
		if(amt < 0){
			Utils.err("Amount must be positive");
			return;
		}
		heat -= amt;
		heat = Math.max(0, heat);
		
		if(burning && heat < BURN_HEAT - TOLERANCE){
			putOutFire();
		}
	}
	
	

	public void update(double dt){
		subtractHeat(COOLDOWN_RATE * dt);
	}
	
	public void manualDraw(Graphics2D g) {
		switch (type) {
		case FLOOR:
		case WALL:
			g.drawImage(Resources.getImage("rocktile.png"), (int) x, (int) y,
					null);
			break;
		default:
			break;
		}

		if(Game.DEBUG){
			g.setColor(Color.white);
			g.drawString(String.format("%.1f", heat), (int)x+24, (int)y+32);
		}
	}

}
