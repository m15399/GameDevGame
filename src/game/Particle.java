package game;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Generic particle - extend to create specialized particles
 */
public class Particle extends MapEntity {

	public double xv, yv;
	public double life;
	
	public Particle(double x, double y, double xv, double yv){
		this.x = x;
		this.y = y;
		this.xv = xv;
		this.yv = yv;
		
		life = 3;
	}
	
	public void update(double dt){
		super.update(dt);
		
		x += xv * dt;
		y += yv * dt;
		
		// Destroy if inside a wall
		Map map = Map.currMap;
		if(map.isWallAt(x, y)){
			destroy();
			return;
		}
		
		// Destroy if lifetime is up
		life -= dt;
		if(life <= 0)
			destroy();
	}
	
	public void draw(Graphics2D g){
		g.setColor(Color.white);
		double size = 5;
		g.fillArc((int)(x-size/2), (int)(y-size/2-15), (int)size, (int)size, 0, 360);
	}
	
}
