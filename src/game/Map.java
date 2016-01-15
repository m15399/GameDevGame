package game;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import network.Client;
import network.TileHeatUpdatesMessage;

import engine.*;
import game.Tile.Type;

/**
 * Holds a 2D array of tiles that represents the map.
 */
public class Map extends GameObject {


	public Image tileset;
	
	private Tile[][] tiles;
	private int width, height;
	
	/**
	 * Create a map from a text file
	 */
	public Map(String textFileName) {
		
		tileset = null;
		
		// Read the file, keep track of width and height, and store each token

		Scanner s = Resources.openFile(textFileName);

		Queue<String> tokens = new LinkedList<String>();
		width = 0;
		height = 0;

		while (s.hasNextLine()) {
			String line = s.nextLine();

			// Skip the line if it's empty or begins with whitespace.
			// This is important in case people put extra blank lines, and as a
			// bonus you can write comments by putting whitespace at the
			// beginning of a line.
			if (line.length() == 0 || Character.isWhitespace(line.charAt(0)))
				continue;

			Scanner l = new Scanner(line);
			
			String firstToken = l.next();
			
			if(firstToken.equals("TILESET")){
				tileset = Resources.getImage(l.next());

			} else {
				// It's a line of tile tokens, add them to 'tokens'

				tokens.add(firstToken);
				width = 1;
				
				while (l.hasNext()) {
					tokens.add(l.next());
					width++;
				}
				height++;
			}
			l.close();
		}
		s.close();

		// Create the map using the tokens we collected
		
		tiles = new Tile[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				String token = tokens.poll();
				tiles[i][j] = new Tile(token, j, i);					
			}
		}

		Utils.log("Created map of size " + width + " x " + height);
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
		return Utils.mod(c, Tile.SIZE);
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
			return t.getType() == Type.WALL;
		else
			return false;
	}
	
	/**
	 * Is the coordinate on the floor?
	 */
	public boolean isFloorAt(double x, double y) {
		Tile t = tileAt(x, y);
		if (t != null)
			return (t.getType() != Type.EMPTY);
		else
			return false;
	}
	
	/**
	 * Is any part of the rect on the floor?
	 */
	public boolean isOnFloor(double x, double y, double w, double h){
		if(isFloorAt(x, y) || isFloorAt(x+w, y) || isFloorAt(x, y+h) || isFloorAt(x+w, y+h)){
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Update the map to reflect the changes in the message
	 */
	public void updateTileHeats(TileHeatUpdatesMessage msg){
		int size = msg.xCoords.size();

		for(int i = 0; i < size; i++){
			int tx = msg.xCoords.get(i);
			int ty = msg.yCoords.get(i);
			int heat = msg.heats.get(i);
			
			Tile tile = tiles[ty][tx];
			tile.serverSetsHeat(heat);
		}
	}
	
	/**
	 * Send all tiles that have been heated up enough to be updates on the server
	 */
	private void sendHeatedTilesToServer(){	
		TileHeatUpdatesMessage updateMsg = new TileHeatUpdatesMessage();
		
		for(int ty = 0; ty < height; ty++){
			for(int tx = 0; tx < width; tx++){
				Tile tile = tiles[ty][tx];
				
				double tileHeatSet = tile.shouldUpdateServerHeat();
				if(tileHeatSet > 0){
					updateMsg.addHeat(tx, ty, tileHeatSet);
					
					// We are sending this info to the server, so go ahead
					// and update our idea of what the server has
					tile.serverSetsHeat(tileHeatSet);
				}
			}
		}
		
		// If any changes to send, send them
		if(updateMsg.xCoords.size() > 0){
			Client.sendMessage(updateMsg);
		}
	}
	
	public void update(double dt){
		// Periodically send heated tiles to server
		if(Globals.isOnlineGame()){
			if(Game.frameNumber % 15 == 0)
				sendHeatedTilesToServer();			
		}
	}

	public void draw(Graphics2D g) {
		// Draw the tiles top to bottom
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[0].length; j++) {
				tiles[i][j].manualDraw(g);
			}
		}
	}

}
