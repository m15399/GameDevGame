package game;

import java.awt.Graphics2D;
import java.awt.Image;

import engine.Entity;
import engine.Utils;

/**
 * Represents a single Tile on the map. Holds its Tile type and determines how
 * to draw it.
 */
public class Tile extends Entity {

	public static final int SIZE = 64;
	
	/**
	 * Type of a tile
	 */
	public enum Type {
		EMPTY, FLOOR, FLOOR_UNBURNABLE, WALL
	}
	
	private Type type;
	
	public int xc, yc;

	private TileFire tileFire;
	private WallVisual wallVisual;
	
	// What we think the server sees
	private double serverHeat;

	/**
	 * Decide how to create a tile based on a given token string
	 */
	public Tile(String token, int xc, int yc) {
		this.type = Type.EMPTY;

		this.xc = xc;
		this.yc = yc;
		x = xc * SIZE;
		y = yc * SIZE;
		
		tileFire = null;
		
		serverHeat = 0;
		
		char c = token.charAt(0);
		switch (c) {
		case '1':
			setType(Type.FLOOR);
			break;
		case '2':
			setType(Type.FLOOR_UNBURNABLE);
			break;
		case 'W':
			setType(Type.WALL);
			break;
		}
	}
	
	public Type getType(){
		return type;
	}
	
	private void setType(Type t){
		if(tileFire != null){
			tileFire.destroy();
			tileFire = null;
		}
		if(wallVisual != null){
			wallVisual.destroy();
			wallVisual = null;
		}
		
		switch (t) {
		case FLOOR:
			tileFire = new TileFire(this, x + Tile.SIZE/2, y + Tile.SIZE/2);
			break;
		case WALL:
			wallVisual = new WallVisual(xc, yc);
			break;
		}
		
		type = t;
	}
	
	/** 
	 * Get what WE see as the heat
	 */
	public double getPlayerHeat(){
		if(tileFire == null)
			return 0;
		else
			return tileFire.getHeat();
	}
	
	/**
	 * Get what the server probably thinks is the heat
	 */
	public double getServerHeat(){
		return serverHeat;
	}
	
	/**
	 * Have we heated the tile up enough to warrant updating the server?
	 * Returns 0 if no, returns our current heat if yes. 
	 */
	public double shouldUpdateServerHeat(){
		// E.g. if heatPerSection is 5, we update the server
		// if our heat is in the bracket of 5 above the server's heat. 
		// (2 and 4 are same section, 2 and 6 are different sections)
		int heatPerSection = 5;
		
		int serverSection = ((int)getServerHeat()) / heatPerSection;
		int playerSection = ((int)getPlayerHeat()) / heatPerSection;
		
		if(playerSection > serverSection){
			return getPlayerHeat();
		} else if (playerSection < serverSection){
			Utils.err("Player thinks tile is cooler than server, how did you do this??");
			return 0;
		} 
		return 0;
	}
	
	/**
	 * Set our current heat and our serverHeat
	 */
	public void serverSetsHeat(double heat){
		if(tileFire != null)
			tileFire.setHeat(heat);
		serverHeat = heat;
	}
	
	/**
	 * Update our heat, but not the serverHeat
	 */
	public void playerAddsHeat(double amt){
		if(amt < 0){
			Utils.err("Heat passed to addHeat should always be positive");
			return;
		}
		
		// tileFire might be null if we are a wall/empty tile
		if(tileFire != null){
			tileFire.changeHeat(amt);
		}
	}
	
	public void burnUp(){
		setType(Type.EMPTY);
	}
	
	public void manualDraw(Graphics2D g) {
		
		// Draw the tile's image from the tileset
		
		Image tileset = Globals.map.tileset;
		
		int xi = (int)x;
		int yi = (int)y;
		int w = (int)(64);
		int h = (int)(76);
		
		switch (type) {
		case FLOOR:
			g.drawImage(tileset, xi, yi, xi + w, yi + h, 64 * 1, 64, 64 * 2, 64 + 76, null);
			break;
			
		case FLOOR_UNBURNABLE:
		case WALL:
			g.drawImage(tileset, xi, yi, xi + w, yi + h, 64 * 3, 64, 64 * 4, 64 + 76, null);
			break;
			
		default:
			break;
		}
	}

}
