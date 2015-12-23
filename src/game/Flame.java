package game;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Flame particle - flies around and burns stuff.
 */
public class Flame extends MapEntity {

	static final double FLAME_LIFE = .6; // How long until particle dies
	
	double xv, yv;
	double life;
	double size;
	
	public Flame(double x, double y, double xv, double yv){
		this.x = x;
		this.y = y;
		this.xv = xv;
		this.yv = yv;
		life = FLAME_LIFE;
		size = 20;
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
		// Alpha depends on how long particle has been alive
		g.setColor(new Color(255,255,100,(int)(100 + 155.0 * life/FLAME_LIFE)));
		
		g.fillArc((int)(x-size/2), (int)(y-size/2-15), (int)size, (int)size, 0, 360);
	}
	
}
