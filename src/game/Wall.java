package game;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import engine.Resources;

/**
 * Draws the wall
 */
public class Wall extends MapEntity {

	public Wall(int xc, int yc){
		x = Tile.SIZE * xc;
		y = Tile.SIZE * yc;
	}
	
	public void update(double dt){
		super.update(dt);
	}
	
	public void draw(Graphics2D g){
		BufferedImage wallImage = Resources.getImage("rockwall.png");
		int dh = wallImage.getHeight() - Tile.SIZE;
		g.drawImage(wallImage, (int)x, (int)y - dh, null);
	}
	
}
