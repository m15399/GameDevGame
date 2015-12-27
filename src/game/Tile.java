package game;

import java.awt.Graphics2D;

import engine.Entity;
import engine.Resources;

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
		EMPTY, FLOOR, WALL
	}
	
	public Type type;

	public int xc, yc;

	private boolean burning;
	private Fire fire;

	/**
	 * Decide how to create a tile based on a given token string
	 */
	public Tile(String token, int xc, int yc) {
		this.type = Type.EMPTY;
		burning = false;
		fire = null;

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
		if(!burning){
			burning = true;
			fire = new Fire(x + SIZE/2, y + SIZE/2);
		}
	}
	
	private void putOutFire(){
		if(burning){
			fire.destroy();
			fire = null;
			burning = false;	
		}	
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

	}

}
