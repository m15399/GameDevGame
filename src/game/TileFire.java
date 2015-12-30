package game;

import java.awt.Color;
import java.awt.Graphics2D;

import engine.Entity;
import engine.Game;
import engine.Utils;
import game.Tile.Type;


/**
 * Controls the fire on a tile
 */
public class TileFire extends Entity {
	
	// How much extra heat is required to bump up/down to the next stage of heat.
	private static final double TOLERANCE = 2;
	
	private enum Stage {
		NONE, EMBER, BURN, FLAME
	}
	
	// Heat required to reach each stage 	
	private static final double[] STAGE_HEATS = {
		0, 10, 30, 50
	};
	
	// Max heat possible
	private static final double HEAT_CAP = 55;

	// Passive heat gain/loss at each stage
	private static final double[] STAGE_HEAT_MODIFY = {
		-2, -1, +1, +1
	};
	
	// On the final stage, tile will burn up and disappear after this amount of time
	private static final double FINAL_STAGE_TIME = 1.5;
	
	
	
	private double heat;
	
	private Stage stage;
	private Entity currEmitter;
	
	private double timeOnFinalStage;
	
	private Tile tile;
	
	
	public TileFire(Tile tile, double x, double y){
		this.x = x;
		this.y = y;
		this.tile = tile;
		
		heat = 0;
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
	
	public void changeHeat(double amt){
		if(amt >= 0){
			addHeat(amt);
		} else {
			subtractHeat(-amt);
		}
	}
	
	public void addHeat(double amt){
		if(amt < 0){
			Utils.err("addHeat argument must be positive");
		}
		
		heat += amt;
		heat = Utils.clamp(heat, 0, HEAT_CAP);
			
		// Do not bump up to next stage if we're on the final stage
		if(stage.ordinal() == Stage.values().length-1)
			return;
			
		// Should we bump up to next stage?
		double checkHeat = STAGE_HEATS[stage.ordinal()+1] + TOLERANCE;
		if(heat > checkHeat)
			setStage(Stage.values()[stage.ordinal()+1]);
	}
	
	public void subtractHeat(double amt){
		if(amt < 0){
			Utils.err("subtractHeat argument must be positive");
		}
		
		heat -= amt;
		heat = Utils.clamp(heat, 0, HEAT_CAP);
		
		// Should we bump down to previous stage?
		double checkHeat = STAGE_HEATS[stage.ordinal()] - TOLERANCE;
		if(heat < checkHeat)
			setStage(Stage.values()[stage.ordinal()-1]);
	}
	
	public void onDestroy(){
		currEmitter.destroy();
	}
	
	public void update(double dt){
		// Passive heat gain/loss
		double mod = STAGE_HEAT_MODIFY[stage.ordinal()];
		changeHeat(mod * dt);
		
		// Burn up if long enough time on final stage
		if(stage.ordinal() == Stage.values().length-1){
			timeOnFinalStage += dt;
			if(timeOnFinalStage >= FINAL_STAGE_TIME){
				tile.setType(Type.EMPTY);
			}
		}
	}
	
	public void draw(Graphics2D g){
		if(Game.DEBUG){
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
			angle = Math.PI/2;
			angleJitter = Math.PI/10;
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
				g.setColor(new Color(255,170,100, alpha));
				
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
			angle = Math.PI/2;
			angleJitter = Math.PI/10;
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
			angle = Math.PI/2;
			angleJitter = Math.PI/10;
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
