package game;

import engine.Entity;
import engine.Game;
import engine.Utils;

/**
 * Generalized particle emitter. Extend from this to make your own emitters
 */
public abstract class Emitter extends Entity {

	public double rate; // particles /sec
	public double jitter; // adds randomness to position
	public double angle; // angle to shoot particles (in degrees)
	public double angleJitter; // adds randomness to angle (in degrees)
	public double velocity; // velocity of particles when emitted
	public double advance; // move the particle a given distance forward when created
	
	// Currently emitting
	private boolean enabled;
	
	// Time last particle was emitted
	private double lastEmit;
	
	public Emitter(){
		rate = 10;
		angle = 0;
		angleJitter = 360;
		jitter = 0;
		velocity = 100;
		advance = 0;
		
		enabled = false;
	}
	
	/**
	 * Override this with your own method
	 */
	public abstract void createParticle(double x, double y, double xv, double yv);
	
	public void setEnabled(boolean b){
		if(enabled && !b){
			disable();
		} else if (!enabled && b){
			enable();
		}
	}
	
	public void enable(){
		enabled = true;

		emitParticle();
		lastEmit = Game.time;
	}
	
	public void disable(){
		enabled = false;
	}
	
	/**
	 * Shoot a single particle particle towards 'angle'
	 */
	public void emitParticle(){
		double a = Utils.randomRange(angle - angleJitter/2, angle + angleJitter/2);
		a = Math.toRadians(a);
		
		double xdir = Math.cos(a);
		double ydir = -Math.sin(a);
		
		double xo = Utils.randomRange(-jitter, jitter);
		double yo = Utils.randomRange(-jitter, jitter);
		
		createParticle(x + xo + xdir * advance, y + yo + ydir * advance, xdir * velocity, ydir * velocity);
	}
	
	public void update(double dt){
		// Shoot some particles if enough time has passed
		if(enabled){
			double timeSinceEmit = Game.time - lastEmit;
			double desiredTime = 1.0 / rate;
			
			// Shoot enough particles to maintain rate
			while(timeSinceEmit > desiredTime){
				timeSinceEmit -= desiredTime;
				lastEmit += desiredTime;
				
				emitParticle();
			}
		}
	}
	
}
