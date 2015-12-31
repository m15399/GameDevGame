package game;

import java.awt.Color;
import java.awt.Graphics2D;

import engine.Entity;
import engine.Game;
import engine.Utils;

/**
 * Shoots flames.
 */
public class FlameThrower extends Emitter {

	/**
	 * Offset from player center - this makes the Flamethrower shoot from the
	 * player's hands instead of his feet. 
	 */
	public static double Y_OFFS = -10;
	
	private Entity parent; // spatial parent
	private double xo, yo; // offset from parent
	
	public FlameThrower(Entity parent){
		this.parent = parent;
		xo = 0;
		yo = Y_OFFS;
		
		// Emitter settings
		rate = 40;
		angleJitter = 30;
		velocity = 400;
		advance = 55;
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
	 * Flame particle - flies around and heats up tiles underneath it. 
	 */
	private class FlameParticle extends Particle {

		private static final double FLAME_LIFE = .5; // How long until particle dies
		
		private static final double TICK_TIME = .15; // Time between ticks of heat
		private static final double AMT_HEAT = TICK_TIME * 5; // Strength of heat
		
		private double lastTick;
		
		public FlameParticle(double x, double y, double xv, double yv){
			super(x, y, xv, yv);

			life = FLAME_LIFE;
			
			tickHeat();
			lastTick = Game.time;
		}
		
		/**
		 * Apply a small amount of heat to the tile we're on
		 */
		private void tickHeat(){			
			Map m = Map.currMap;
			Tile t = m.tileAt(x, y);
			if(t != null)
				t.addHeat(AMT_HEAT);
		}
		
		public void update(double dt){
			super.update(dt);
			
			if(Game.time > lastTick + TICK_TIME){
				tickHeat();
				lastTick += TICK_TIME;
			}
		}
		
		@Override
		public void draw(Graphics2D g){
			// Alpha depends on how long particle has been alive
			int alpha = (int)(Utils.lerp(60, 255, life/FLAME_LIFE));
			g.setColor(new Color(255,255,100, alpha));
			
			double size = 20;
			g.fillArc((int)(x-size/2), (int)(y-size/2), (int)size, (int)size, 0, 360);
		}
		
	}
	
}
