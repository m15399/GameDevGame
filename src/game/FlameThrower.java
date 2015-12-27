package game;

import java.awt.Color;
import java.awt.Graphics2D;

import engine.Entity;
import engine.Utils;

/**
 * Shoots flames.
 */
public class FlameThrower extends Emitter {

	private Entity parent; // spatial parent
	private double xo, yo; // offset from parent
	
	public FlameThrower(Entity parent){
		this.parent = parent;
		xo = yo = 0;
		
		// Emitter settings
		rate = 40;
		angleJitter = Math.PI/12;
		velocity = 400;
		advance = 30;
	}
	
	public void setFiring(boolean b){
		setEnabled(b);
	}
	
	public void emitParticle(){
		// Follow parent object
		x = parent.x + xo;
		y = parent.y + yo;
		
		super.emitParticle();
	}
	

	@Override
	public void createParticle(double x, double y, double xv, double yv) {
		new FlameParticle(x, y, xv, yv);
	}
	
	/**
	 * Flame particle - flies around and burns stuff.
	 */
	private class FlameParticle extends Particle {

		private static final double FLAME_LIFE = .6; // How long until particle dies

		private double size;
		
		public FlameParticle(double x, double y, double xv, double yv){
			super(x, y, xv, yv);

			life = FLAME_LIFE;
			size = 20;
		}
		
		@Override
		public void draw(Graphics2D g){
			// Alpha depends on how long particle has been alive
			int alpha = (int)(Utils.lerp(60, 255, life/FLAME_LIFE));
			g.setColor(new Color(255,255,100, alpha));
			
			// We subtract 15 to make it look like it's floating
			g.fillArc((int)(x-size/2), (int)(y-size/2-15), (int)size, (int)size, 0, 360);
		}
		
	}
	
}