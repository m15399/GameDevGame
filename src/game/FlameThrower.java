package game;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.sound.sampled.Clip;

import utils.Utils;

import engine.Game;
import engine.Sound;

/**
 * Shoots flames.
 */
public class FlameThrower extends Emitter {

	/**
	 * How powerful the flamethrower is
	 */
	private static final double POWER = 3.6;
	
	// Overheat controls
	private static final double HEATUP_RATE = .23;
	private static final double COOLDOWN_RATE = HEATUP_RATE;
	private static final double OVERHEAT_DURATION = 2;
	
	// Particle settings
	private static final double FLAME_LIFE = .5; // How long until particle dies
	private static final double TICK_TIME = .15; // Time between ticks of heat
	private static final double PARTICLE_HEAT_AMT = TICK_TIME * FlameThrower.POWER; // Strength of heat

	
	
	/**
	 * Offset from player center - this makes the Flamethrower shoot from the
	 * player's hands instead of his feet. 
	 */
	public static final double Y_OFFS = -10;
	
	private Player parent; 
	private double xo, yo; // offset from parent
	
	private double overheatTime;
	private double heat; 
	
	private Clip soundEffect = null;
	
	public FlameThrower(Player parent){
		this.parent = parent;
		xo = 0;
		yo = Y_OFFS;
		
		// Emitter settings
		rate = 40;
		angleJitter = 30;
		velocity = 400;
		advance = 55;
		
		overheatTime = 0;
		heat = 0;
	}
	
	public double getHeatFraction(){
		return heat;
	}
	
	public boolean isOverheating(){
		return overheatTime != 0;
	}
	
	private void stopSoundEffect(){
		if(soundEffect != null){
			soundEffect.stop();
			soundEffect = null;
		}
	}
	
	public void setFiring(boolean b){
		if(b && !getEnabled() && overheatTime == 0){
			stopSoundEffect();
			if(!parent.isDummy())
				soundEffect = Sound.startLoop("flamethrower.wav");
			enable();
		} else if(!b && getEnabled()){
			stopSoundEffect();
			disable();
		}
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
	
	public void update(double dt){
		super.update(dt);
		
		if(getEnabled()){
			// Heat up
			heat += HEATUP_RATE * dt;
			
			// Overheated
			if(heat >= 1){
				heat = 1;
				overheatTime = OVERHEAT_DURATION; 
				setFiring(false);
			}
		} else {
			// Cool down
			heat -= COOLDOWN_RATE * dt;
			if(heat < 0)
				heat = 0;
			
			// If we're overheated
			if(overheatTime != 0){
				overheatTime -= dt;
				
				// Done overheating
				if(overheatTime < 0)
					overheatTime = 0;
			}
		}
	}
	
	public void onDestroy(){
		stopSoundEffect();
	}
	
	/**
	 * Flame particle - flies around and heats up tiles underneath it. 
	 */
	private class FlameParticle extends Particle {
		
		private double lastTick;
		
		public FlameParticle(double x, double y, double xv, double yv){
			super(x, y, xv, yv);

			life = FLAME_LIFE;
			
			tickHeat();
			lastTick = Game.getTime();
		}
		
		/**
		 * Apply a small amount of heat to the tile we're on
		 */
		private void tickHeat(){
			// Don't heat tiles if we're not authoritative
			if(!Globals.isAuthoritative())
				return; 
			
			Map m = Globals.map;
			Tile t = m.tileAt(x, y);
			if(t != null)
				t.authoritativeAddsHeat(PARTICLE_HEAT_AMT);
		}
		
		public void update(double dt){
			super.update(dt);
			
			if(Game.getTime() > lastTick + TICK_TIME){
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
