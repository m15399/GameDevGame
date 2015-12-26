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
	enum Type {
		EMPTY, FLOOR, WALL
	}

	/**
	 * Decide how to create a tile based on a given token string
	 */
	static Tile create(String token, int xc, int yc) {

		Tile tile = new Tile(xc, yc);

		char c = token.charAt(0);
		switch (c) {

		case 'f':
			tile.catchFire();
		case '1':
			tile.type = Type.FLOOR;
			break;
		case '2':
			tile.type = Type.WALL;

			// Create a wall object on top of wall tiles
			new Wall(xc, yc);
			break;
		}

		return tile;
	}

	Type type;

	int xc, yc;

	boolean burning;
	Fire fire;

	public Tile(int xc, int yc) {
		this.type = Type.EMPTY;
		burning = false;
		fire = null;

		this.xc = xc;
		this.yc = yc;
		x = xc * SIZE;
		y = yc * SIZE;

	}

	public void catchFire() {
		burning = true;
		fire = new Fire(x + SIZE/2, y + SIZE/2);

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
