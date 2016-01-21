package game;

import java.awt.Graphics2D;
import java.awt.Image;

import network.MapStateMessage;
import network.TileUpdatesMessage;

import engine.Entity;

/**
 * Represents a single Tile on the map. Holds its Tile type and determines how
 * to draw it.
 */
public class Tile extends Entity {

	public static final int SIZE = 64;
	
	private static final double RESPAWN_DELAY = 15;
	private static final double RESPAWN_GRAPHIC_TIME = 3;
	private static final int RESPAWN_GRAPHIC_SIZE = 8;
	
	/**
	 * Type of a tile
	 */
	public enum Type {
		EMPTY, RESPAWNING, FLOOR, FLOOR_UNBURNABLE, WALL, NO_CHANGE
	}
	
	private Type type;
	
	/**
	 * What we think the type of this tile is on the network
	 */
	private Type networkType;
	
	public int xc, yc;

	private TileFire tileFire;
	private WallVisual wallVisual;
	
	/**
	 * Should we respawn if we're empty? This usually means the tile was
	 * originally set to FLOOR, and should respawn after burning up
	 */
	private boolean shouldRespawn;
	private double respawnTime;
	
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
		
		shouldRespawn = false;
				
		char c = token.charAt(0);
		switch (c) {
		case '1':
			setType(Type.FLOOR);
			shouldRespawn = true;
			break;
		case '2':
			setType(Type.FLOOR_UNBURNABLE);
			break;
		case 'W':
			setType(Type.WALL);
			break;
		}

		// When created from a map file token, we can assume the network type 
		// is the same as the local type
		networkType = type;

	}
	
	public Type getType(){
		return type;
	}
	
	private void setType(Type t){
		if(type == t)
			return;
		
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
		case EMPTY:
			respawnTime = RESPAWN_DELAY;
			break;
		}
		
		type = t;
	}
	
	public double getHeat(){
		if(tileFire != null)
			return tileFire.getHeat();
		else
			return 0;
	}
	
	private void networkSetsType(Type t){
		if(t == Type.NO_CHANGE || t == type)
			return;
				
		setType(t);
		networkType = t;
	}
	
	/**
	 * Network is updating us, set our current heat and our networkHeat
	 */
	private void networkSetsHeat(double heat){
		if(tileFire != null && heat != -1)
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
	
	public void writeUpdate(TileUpdatesMessage msg){
		
		int tileHeatSet = getNextHeatUpdate();
		Type sendType = Type.NO_CHANGE;
		boolean shouldSend = false;
		
		// Only send our type if we are the server
		if(Globals.isServer()){
			sendType = type;
			
			// Server should update tile type
			if(networkType != type){
				shouldSend = true;
				networkType = type;
			}
		}
		
		if(tileHeatSet >= 0)
			shouldSend = true;
		
		if(shouldSend)
			msg.addUpdate(xc, yc, sendType, tileHeatSet);
	}
	
	public void writeState(MapStateMessage msg){
		msg.types.add(getType());
		msg.heats.add((int)getHeat());
	}
	
	public void receiveUpdate(Type t, int h){
		networkSetsType(t);
		networkSetsHeat(h);
	}
	
	/**
	 * Get the heat we should send to the network. Returns -1 if not enough heat change,
	 * otherwise returns the current heat we should send
	 */
	private int getNextHeatUpdate(){
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
		
		case RESPAWNING:
			// Draw a shrunken version of the tile
			int xShrink = (SIZE - RESPAWN_GRAPHIC_SIZE);
			int yShrink = (int)(xShrink * ((double)h / w));
			w -= xShrink;
			h -= yShrink;
			xi += xShrink/2;
			yi += yShrink/2;
			g.drawImage(tileset, xi, yi, xi + w, yi + h, 64 * 1, 64, 64 * 2, 64 + 76, null);
			break;
		
		default:
			break;
		}
	}
	
	public void update(double dt){
		// Respawn timer
		// We go from empty -> respawning -> floor
		if(shouldRespawn && !Globals.isOnlineClient()){
			if(type == Type.EMPTY){
				respawnTime -= dt;
				if(respawnTime <= RESPAWN_GRAPHIC_TIME)
					setType(Type.RESPAWNING);	
				
			} else if (type == Type.RESPAWNING){
				respawnTime -= dt;
				if(respawnTime <= 0)
					setType(Type.FLOOR);
			}
		}
	}

}
