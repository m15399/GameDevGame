package game;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import engine.*;
import game.Tile.Type;

/**
 * Holds a 2D array of tiles that represents the map.
 */
public class Map extends GameObject {

	Tile[][] tiles;

	/**
	 * Create a map from a text file
	 */
	public Map(String textFileName) {

		// Read the file, keep track of width and height, and store each token

		Scanner s = Resources.getFile(textFileName);

		Queue<String> tokens = new LinkedList<String>();
		int w = 0, h = 0;

		while (s.hasNextLine()) {
			String line = s.nextLine();

			// Skip the line if it's empty or begins with whitespace.
			// This is important in case people put extra blank lines, and as a
			// bonus you can write comments by putting whitespace at the
			// beginning of a line.
			if (line.length() == 0 || Character.isWhitespace(line.charAt(0)))
				continue;

			h++;

			// Add tokens of this line to 'tokens'
			Scanner l = new Scanner(line);
			while (l.hasNext()) {
				tokens.add(l.next());
				if (h == 1)
					w++;
			}
			l.close();
		}
		s.close();

		// Create the map using the tokens we collected

		tiles = new Tile[h][w];
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				tiles[i][j] = new Tile(tokens.poll());
			}
		}

		System.out.println("Created map of size " + w + " x " + h);
	}

	/**
	 * Return the tile at the given coordinate, or null if it's outside the
	 * map's bounds.
	 */
	public Tile tileAt(double x, double y) {
		int tx = (int) Math.floor(x / Tile.SIZE);
		int ty = (int) Math.floor(y / Tile.SIZE);
		if (tx < 0 || tx >= tiles[0].length || ty < 0 || ty >= tiles.length)
			return null;
		else
			return tiles[ty][tx];
	}

	/**
	 * How far is the coordinate into the tile? (in pixels)
	 */
	public double distIntoTile(double c) {
		return c % Tile.SIZE;
	}

	/**
	 * How much of the tile is left after going 'c' pixels into the tile?
	 */
	public double distLeftInTile(double c) {
		return Tile.SIZE - distIntoTile(c);
	}

	/**
	 * Is the coordinate inside a wall?
	 */
	public boolean isWallAt(double x, double y) {
		Tile t = tileAt(x, y);
		if (t != null)
			return t.type == Type.WALL;
		else
			return false;
	}

	public void update(double dt) {

	}

	public void draw(Graphics2D g) {
		// Draw all the tiles
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[0].length; j++) {
				tiles[i][j].draw(g, j, i);
			}
		}
	}

}