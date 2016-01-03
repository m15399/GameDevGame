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
	
	public void addHeat(double amt){
		if(amt < 0){
			Utils.err("Heat passed to addHeat should always be positive");
			return;
		}
		
		if(tileFire != null)
			tileFire.addHeat(amt);
	}
	
	public void burnUp(){
		setType(Type.EMPTY);
	}
	
	public void manualDraw(Graphics2D g) {
		
		// Draw the tile's image from the tileset
		
		Image tileset = GameDevGame.map.tileset;
		
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
