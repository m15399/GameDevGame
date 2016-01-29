package game;

import java.awt.Color;
import java.awt.Graphics2D;

import utils.Utils;

import engine.Entity;
import engine.Game;


/**
 * Controls the fire on a tile
 */
public class TileFire extends Entity {
	
	// How fast the heat passively changes
	private static final double PASSIVE_HEAT_CHANGE_SPEED = 1; 
	
	// On the final stage, tile will burn up and disappear after this amount of time
	private static final double FINAL_STAGE_TIME = 1.15;
	
	// How much extra heat is required to bump down to the next stage of heat.
	private static final double TOLERANCE = 3;
	
	private enum Stage {
		NONE, EMBER, BURN, FLAME
	}
	
	// Heat required to reach each stage 
	private static final double[] STAGE_HEATS = {
		0, 6, 12, 21
	};
	
	// Max heat possible
	private static final double HEAT_CAP = STAGE_HEATS[STAGE_HEATS.length-1] + 5;

	// Passive heat gain/loss at each stage (in heat per second, but multiplied by HEAT_CHANGE_SPEED)
	private static final double[] STAGE_HEAT_MODIFY = {
		-1, -1, +1, +1
	};

	
	
	
	private double heat;
	
	/**
	 * If we are a client, this represents what we think the server's heat is.
	 * If we are the server, this represents what we think the clients' heats are. 
	 */
	private double networkHeat;
	
	/**
	 * When we last applied heat to the tile
	 */
	private double lastHeatAppliedTime;
	
	/**
	 * Last time the heat was changed at all
	 */
	private double lastHeatChangeTime;
	
	private Stage stage;
	private Entity currEmitter;
	
	private double timeOnFinalStage;
	
	private Tile tile;
	
	
	public TileFire(Tile tile, double x, double y){
		this.x = x;
		this.y = y;
		this.tile = tile;
		
		heat = 0;
		networkHeat = 0;
		lastHeatAppliedTime = -1;
		lastHeatChangeTime = -1;

		stage = Stage.NONE;
		currEmitter = null;
		timeOnFinalStage = 0;
	}

	private void setStage(Stage s){
		if(stage == s){
			return;
		}
				
		if(currEmitter != null){
			currEmitter.destroy();
			currEmitter = null;
		}
		
		stage = s;
		timeOnFinalStage = 0;
		
		switch(stage){
		case EMBER:
			currEmitter = new EmberEmitter(x, y);
			break;
		case BURN:
			currEmitter = new FireEmitter(x, y);
			break;
		case FLAME:
			currEmitter = new FlameEmitter(x, y);
			break;
		}
	}
	
	public double getHeat(){
		return heat;
	}

	/**
	 * Call this when the local player takes an action that adds heat. 
	 */
	public void localPlayerAddsHeat(double amt){
		if(amt < 0){
			Utils.err("Added heat amt should always be positive");
			return;
		}
		changeHeat(amt);
		lastHeatAppliedTime = Game.time;
	}
	
	private void setHeat(double heat){
		changeHeat(heat - this.heat);
	}
	
	private void changeHeat(double amt){
		passiveChangeHeat(amt);
	
		lastHeatChangeTime = Game.time;
	}
	
	/**
	 * Changes the heat without changing lastHeatChangeTime
	 */
	private void passiveChangeHeat(double amt){
		heat += amt;
		heat = Utils.clamp(heat, 0, HEAT_CAP);
				
		if(amt >= 0){
			tryHeatUp();
		} else {
			tryCoolDown();
		}
	}
	
	/**
	 * Moves up stages until we're on the correct stage for current heat
	 */
	private void tryHeatUp(){
		// Do not bump up to next stage if we're on the final stage
		if(stage.ordinal() == Stage.values().length-1)
			return;
			
		// Should we bump up to next stage?
		double checkHeat = STAGE_HEATS[stage.ordinal()+1];
		if(heat >= checkHeat){
			setStage(Stage.values()[stage.ordinal()+1]);
			tryHeatUp();
		} else {
			return;
		}
	}
	
	/**
	 * Moves down stages until we're on the correct stage for current heat
	 */
	private void tryCoolDown(){
		// Should we bump down to previous stage?
		double checkHeat = STAGE_HEATS[stage.ordinal()] - TOLERANCE;
		if(heat <= checkHeat){
			setStage(Stage.values()[stage.ordinal()-1]);
			tryCoolDown();
		}
	}
	
	/**
	 * Get the heat we should send to the network. Returns -1 if not enough heat change,
	 * otherwise returns the current heat we should send
	 */
	public int getNextHeatUpdate(){
		// Figure out what section (or bracket) our heat and network heat is in
		
		// E.g. if heatPerSection is 5, we update the network
		// if our heat is in the bracket of 5 above the network's heat. 
		// (2 and 4 are same section, 2 and 6 are different sections)
		
		// One special case: 0 is considered section -1. This is because the difference
		// between heat = 4 and heat = 0 is quite significant and we would want to update
		// the network to heat = 0. 
		
		int heatPerSection = 3;
		int localSection, networkSection;
		
		if(networkHeat == 0)
			networkSection = -1;
		else 
			networkSection = ((int)networkHeat) / heatPerSection;
		
		if(getHeat() == 0)
			localSection = -1;
		else 
			localSection = ((int)getHeat()) / heatPerSection;
		
		
		// Should we update the network?
		
		boolean shouldUpdate = false;
		
		if(Globals.isOnlineClient()){
			if(localSection > networkSection){
				shouldUpdate = true;
			} else if (localSection < networkSection){
				Utils.err("Player thinks tile is cooler than network, this shouldn't be possible??");
			} 
		} else if(Globals.isServer()){
			if(localSection != networkSection)
				shouldUpdate = true;
		}
		
		
		// Return the update or -1
		
		if(shouldUpdate){
			// We're going to send this heat to the network, so we can assume the
			// network now has our current heat
			int retVal = (int)Math.floor(getHeat());
			networkHeat = retVal;
			return retVal;
			
		} else {
			return -1;			
		}
	}
	
	/**
	 * Network is updating us, set our current heat and our networkHeat
	 */
	public void networkSetsHeat(double heatVal){
		if(Globals.isOnlineClient()){
			// If we're the local player and we have heated the tile up
			// recently, favor our version
			if(heatVal > getHeat() || Game.time - lastHeatAppliedTime > 1.5)
				setHeat(heatVal);
			
			// Client can assume whole network has been set to this heat
			networkHeat = heatVal;
		} else {
			setHeat(heatVal);
		}
	}
	
	public void onDestroy(){
		if(currEmitter != null){
			currEmitter.destroy();
			currEmitter = null;
		}
	}
	
	public void update(double dt){
		if(Globals.isOnlineClient())
			return;
		
		// Passive heat gain/loss
		// Wait .5s because we don't want the heat to cool down before we have a 
		// chance to update the network (makes sure network sees the max heat 
		// reached before cooling)
		if(Game.time - lastHeatChangeTime > .5){
			double mod = STAGE_HEAT_MODIFY[stage.ordinal()] * PASSIVE_HEAT_CHANGE_SPEED;
			passiveChangeHeat(mod * dt);
		}
		
		// Burn up if long enough time on final stage
		if(stage.ordinal() == Stage.values().length-1){
			timeOnFinalStage += dt;
			if(timeOnFinalStage >= FINAL_STAGE_TIME){
				tile.burnUp();
			}
		}
	}
	
	public void draw(Graphics2D g){
		if(Globals.DEV_MODE){
			g.setColor(Color.white);
			g.drawString(String.format("%.1f", heat), (int)x-10, (int)y+4);
		}
	}
	
	
	
	//
	// Emitters for the different stages
	//
	
	private class EmberEmitter extends Emitter {
		public EmberEmitter(double x, double y){
			this.x = x;
			this.y = y;
			
			rate = 20;
			angle = 90;
			angleJitter = 36;
			jitter = Tile.SIZE/2 - 10;
			velocity = 40;
			advance = 0;
			
			enable();
		}
		
		public void createParticle(double x, double y, double xv, double yv) {
			new EmberParticle(x, y, xv, yv);
		}
		
		private class EmberParticle extends Particle{
			private static final double LIFE = .2; 

			public EmberParticle(double x, double y, double xv, double yv){
				super(x, y, xv, yv);
				life = LIFE;
			}
			
			public void draw(Graphics2D g){
				// Alpha depends on how long particle has been alive
				int alpha = (int)(Utils.lerp(60, 255, life/LIFE));
				g.setColor(new Color(255,190,100, alpha));
				
				double size = 5;
				g.fillArc((int)(x-size/2), (int)(y-size/2), (int)size, (int)size, 0, 360);
			}	
		}
	}
	
	
	
	private class FireEmitter extends Emitter {
		public FireEmitter(double x, double y){
			this.x = x;
			this.y = y;
			
			rate = 30;
			angle = 90;
			angleJitter = 36;
			jitter = Tile.SIZE/2 - 10;
			velocity = 60;
			advance = 0;
			
			enable();
		}
		
		public void createParticle(double x, double y, double xv, double yv) {
			new FireParticle(x, y, xv, yv);
		}
		
		private class FireParticle extends Particle{
			private static final double LIFE = .3; 
			
			public FireParticle(double x, double y, double xv, double yv){
				super(x, y, xv, yv);
				life = LIFE;
			}
			
			public void draw(Graphics2D g){
				// Alpha depends on how long particle has been alive
				int alpha = (int)(Utils.lerp(60, 255, life/LIFE));
				g.setColor(new Color(255,255,100, alpha));

				double size = 10;
				g.fillArc((int)(x-size/2), (int)(y-size/2), (int)size, (int)size, 0, 360);
			}
		}
	}
	
	private class FlameEmitter extends Emitter {
		public FlameEmitter(double x, double y){
			this.x = x;
			this.y = y;
			
			rate = 80;
			angle = 90;
			angleJitter = 36;
			jitter = Tile.SIZE/2 - 10;
			velocity = 150;
			advance = 0;
			
			enable();
		}
		
		public void createParticle(double x, double y, double xv, double yv) {
			new FlameParticle(x, y, xv, yv);
		}
		
		private class FlameParticle extends Particle{
			private static final double LIFE = .15; 
			
			public FlameParticle(double x, double y, double xv, double yv){
				super(x, y, xv, yv);
				life = LIFE;
			}
			
			public void draw(Graphics2D g){
				// Alpha depends on how long particle has been alive
				int alpha = (int)(Utils.lerp(60, 255, life/LIFE));
				g.setColor(new Color(255,255,100, alpha));

				double size = 20;
				g.fillArc((int)(x-size/2), (int)(y-size/2), (int)size, (int)size, 0, 360);
			}
		}
	}
	
}
