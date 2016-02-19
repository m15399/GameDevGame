package game;

import java.awt.Graphics2D;
import java.awt.Image;

/**
 * Draws the wall
 */
public class WallVisual extends MapEntity {
	
	public WallVisual(int xc, int yc){
		x = Tile.SIZE * (xc + .5);
		y = Tile.SIZE * (yc + .5);
	}
	
	public void update(double dt){
		super.update(dt);
	}
	
	public void draw(Graphics2D g){
		Image tileset = Globals.map.tileset;
		g.drawImage(tileset, (int) x-32, (int) (y-64), (int)(x + 32), (int)(y + 32), 
					64 * 5, 32, 64 * 6, 64 * 2, null);
	}	
}
