package game;

import java.awt.Color;
import java.awt.Graphics2D;

import engine.Utils;


/**
 * A fire happening on a tile
 */
public class Fire extends Emitter {

	public Fire(double x, double y){
		this.x = x;
		this.y = y;
		
		rate = 30;
		angle = Math.PI/2;
		angleJitter = Math.PI/10;
		jitter = Tile.SIZE/2 - 10;
		velocity = 80;
		advance = 0;
		
		enable();
	}
	
	@Override
	public void createParticle(double x, double y, double xv, double yv) {
		new FireParticle(x, y, xv, yv);
	}
	
	private class FireParticle extends Particle{
		
		private static final double FIRE_PARTICLE_LIFE = .3; 

		private double size;
		
		public FireParticle(double x, double y, double xv, double yv){
			super(x, y, xv, yv);

			life = FIRE_PARTICLE_LIFE;
			size = 10;
		}
		
		@Override
		public void draw(Graphics2D g){
			// Alpha depends on how long particle has been alive
			int alpha = (int)(Utils.lerp(60, 255, life/FIRE_PARTICLE_LIFE));
			g.setColor(new Color(255,255,100, alpha));
			
			g.fillArc((int)(x-size/2), (int)(y-size/2), (int)size, (int)size, 0, 360);
		}
		
	}
	
}
