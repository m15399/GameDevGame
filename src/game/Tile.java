package game;

import java.awt.Graphics2D;
import java.awt.Image;

import engine.Entity;

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
	 * Network is updating us, set our current heat and our networkHeat
	 */
	public void networkSetsHeat(double heat){
		if(tileFire != null)
			tileFire.networkSetsHeat(heat);
	}
	
	/**
	 * Update our heat, but not the networkHeat
	 */
	public void localPlayerAddsHeat(double amt){
		// tileFire might be null if we are a wall/empty tile
		if(tileFire != null){
			tileFire.localPlayerAddsHeat(amt);
		}
	}
	
	/**
	 * Get the heat we should send to the network. Returns -1 if not enough heat change,
	 * otherwise returns the current heat we shoudl send
	 */
	public int getNextHeatUpdate(){
		if(tileFire == null)
			return -1;
		else 
			return tileFire.getNextHeatUpdate();
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
