package game;

import java.awt.Color;
import java.awt.Graphics2D;

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

	public Tile() {
		this.type = Type.EMPTY;
	}

	public Tile(String type) {
		this.type = getType(type); // yo dawg, I hear you like types
	}

	public void draw(Graphics2D g, int xc, int yc) {
		switch (type) {
		case FLOOR:
			g.setColor(Color.gray);
			g.fillRect(xc * SIZE + 1, yc * SIZE + 1, SIZE - 2, SIZE - 2);
			break;
		case WALL:
			g.setColor(Color.lightGray);
			g.fillRect(xc * SIZE + 1, yc * SIZE + 1, SIZE - 2, SIZE - 2);
			break;
		default:
			break;
		}

	}

}
