package game;

import java.awt.Graphics2D;

import engine.Resources;

/**
 * Represents a single Tile on the map. Holds its Tile type and determines how to
 * draw it.
 */
public class Tile {

	public static final int SIZE = 64;
	
	/**
	 * Type of a tile
	 */
	enum Type {
		EMPTY, FLOOR, WALL
	}

	/**
	 * Convert a String to a Type
	 */
	static Type getType(String s) {
		char c = s.charAt(0);
		switch (c) {
		case '1':
			return Type.FLOOR;
		case '2':
			return Type.WALL;
		default:
			return Type.EMPTY;
		}
	}

	Type type;


	public Tile(Type type) {
		this.type = type;
	}

	public void draw(Graphics2D g, int xc, int yc) {
		
		int x = xc * SIZE;
		int y = yc * SIZE;
		
		switch (type) {
		case FLOOR:
		case WALL:
			g.drawImage(Resources.getImage("rocktile.png"), x, y, null);
			break;
		default:
			break;
		}

	}

}
