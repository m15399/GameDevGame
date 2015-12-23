package game;

import engine.Entity;
import engine.Game;
import engine.Utils;

/**
 * Emits Flame particles towards a particular angle.
 */
public class FlameThrower extends Entity {

	static final double RATE_OF_FIRE = 40;
	static final double SPREAD = Math.PI/12;
	static final double VELOCITY = 400; // of particles
	static final double DISTANCE = 30; // distance away from (x,y) that particles are emitted
	
	Entity parent; // spatial parent
	double xo, yo; // offset from parent
	
	boolean firing;
	double angle;
	
	double lastFire;
	
	public FlameThrower(Entity parent){
		this.parent = parent;
		xo = yo = 0;
	}
	
	public void setFiring(boolean b){
		if(firing && !b){
			ceaseFire();
		} else if (!firing && b){
			fire();
		}
	}
	
	public void fire(){
		firing = true;

		shootFlame();
		lastFire = Game.time;
	}
	
	public void ceaseFire(){
		firing = false;
	}
	
	/**
	 * Shoot a single flame particle towards 'angle'
	 */
	void shootFlame(){		
		double a = Utils.randomRange(angle - SPREAD, angle + SPREAD);
		
		double xdir = Math.cos(a);
		double ydir = Math.sin(a);
				
		new Flame(x + xo + xdir * DISTANCE, y + yo + ydir * DISTANCE, xdir * VELOCITY, ydir * VELOCITY);
	}
	
	public void update(double dt){
		x = parent.x + xo;
		y = parent.y + yo;
		
		// Shoot some particles if enough time has passed
		if(firing){
			double timeSinceFire = Game.time - lastFire;
			double desiredTime = 1.0 / RATE_OF_FIRE;
			
			// Shoot enough particles to maintain rate of fire
			while(timeSinceFire > desiredTime){
				timeSinceFire -= desiredTime;
				lastFire += desiredTime;
				
				shootFlame();
			}
		}
	}
	
	
	
}
