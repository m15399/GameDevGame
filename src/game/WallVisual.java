package game;

import java.awt.Graphics2D;
import java.awt.Image;

/**
 * Draws the wall
 */
public class WallVisual extends MapEntity {

	public WallVisual(int xc, int yc){
		x = Tile.SIZE * xc;
		y = Tile.SIZE * yc;
	}
	
	public void update(double dt){
		super.update(dt);
	}
	
	public void draw(Graphics2D g){
		Image tileset = GameDevGame.map.tileset;
		g.drawImage(tileset, (int) x, (int) (y-32), (int)(x + 64), (int)(y + 64), 
					64 * 5, 32, 64 * 6, 64 * 2, null);
	}	
}
