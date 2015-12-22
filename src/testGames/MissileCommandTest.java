package testGames;

import java.awt.*;
import java.util.ArrayList;

import javax.sound.sampled.Clip;

import engine.*;

public class MissileCommandTest extends GameObject {
	
	public static void main(String[] args) {
		Application.launch();
		new MissileCommandTest();
	}
	
	
	
	class Player extends GameObject {
		
		static final double RELOAD_TIME = 1.0;
	
		double x = Game.WIDTH/2;
		double y = Game.HEIGHT - 40;
		
		double reloadTimeLeft = 0;
		
		public void update(double dt){
			
			// process input
			reloadTimeLeft -= dt;
			if(Input.isMousePressed() && reloadTimeLeft <= 0){
				reloadTimeLeft = RELOAD_TIME;
				
				// fire missile
				new Missile(x, y, Input.getMouseLoc().x, Input.getMouseLoc().y);
			}
		}
		
		public void draw(Graphics2D g){
			g.setColor(Color.white);
			Utils.fillRect(g, x, y, 20, 20, 0, 1);
		}
		
	}
	
	class Missile extends GameObject {
		
		static final double MISSILE_SPEED = 150; 
		
		double x, y; // location
		double xv, yv; // velocity
		
		double targetX, targetY; // where we're flying to
		
		double lastDist;
		
		Color color;
		
		public Missile(double x, double y, double targetX, double targetY){
			tag = "Missile";
			
			this.x = x;
			this.y = y;
			this.targetX = targetX;
			this.targetY = targetY;

			// set velocity 
			double dist = distanceToTarget();
			xv = (targetX - x) / dist * MISSILE_SPEED;
			yv = (targetY - y) / dist * MISSILE_SPEED;
			
			lastDist = 9999;
			
			color = Color.green;
		}
		
		double distanceToTarget(){
			double dx = targetX - x;
			double dy = targetY - y;
			return Math.sqrt(dx * dx + dy * dy);
		}
		
		public void update(double dt){
			x += xv * dt;
			y += yv * dt;
			
			double dist = distanceToTarget();
			if(dist > lastDist){ // now we've passed our target
				createExplosion();
				destroy();
			} 
			lastDist = dist;
			
			// randomly spawn particles on our trail for smoke effect
			if(Utils.randomRange(0, 1.0) < .25){
				int spread = 2;
				new TrailParticle(x + Utils.randomRangeInt(-spread, spread), 
						y + Utils.randomRangeInt(-spread, spread));
			}
		}
		
		public void createExplosion(){
			new Explosion(targetX, targetY, false);
		}
		
		public void draw(Graphics2D g){
			g.setColor(color);
			Utils.fillRect(g, x, y, 10, 10, 0, 1);
			
			// draw crosshairs where our target is
			g.setColor(Color.white);
			double ls = 5;
			g.drawLine((int)(targetX - ls), (int)targetY, (int)(targetX + ls), (int)targetY);
			g.drawLine((int)targetX, (int)(targetY - ls), (int)targetX, (int)(targetY + ls));
		}
		
	}
	
	class EnemyMissile extends Missile {
		public EnemyMissile(double x, double y, double targetX, double targetY){
			super(x, y, targetX, targetY);
			
			color = Color.orange;
			
			tag = "EnemyMissile";
			
			// make enemy missiles much slower
			double slowDown = 3;
			xv /= slowDown;
			yv /= slowDown;
		}
		
		public void createExplosion(){
			new Explosion(targetX, targetY, true);
		}
	}
	
	class TrailParticle extends GameObject {
		double x, y;
		
		double timeLeft = 1; // dies after 1 sec
		
		public TrailParticle(double x, double y){
			this.x = x;
			this.y = y;
			setDrawOrder(-1);
		}
		
		public void update(double dt){
			timeLeft -= dt;
			if(timeLeft < 0)
				destroy();
		}
		
		public void draw(Graphics2D g){
			g.setColor(Color.darkGray);
			double s = 6;
			Utils.fillRect(g, x, y, s, s, 0, 1);
		}
	}
	
	class Explosion extends GameObject {
		
		double x, y;
		double timeLeft;
		int size;
		boolean isEnemy;
		
		public Explosion(double x, double y, boolean isEnemy) {
			this.x = x;
			this.y = y;
			
			timeLeft = 1;
			size = 100;
			
			this.isEnemy = isEnemy;
		}
		
		public void update(double dt){
			
			if(!isEnemy){
				// destroy enemy missiles in our range
				ArrayList<GameObject> enemyMissiles = GameObject.findObejctsByTag("EnemyMissile");
				for(GameObject o : enemyMissiles){
					Missile m = (Missile) o;
					
					// calc distance from explosion to missile
					double dx = m.x - x;
					double dy = m.y - y;
					double dist = Math.sqrt(dx * dx + dy * dy);
					
					// if missile is in our radius, destroy it
					if(dist < size/2){
						m.destroy();
					}
				}
			}
			
			// destroy explosion when time runs out
			timeLeft -= dt;
			if(timeLeft <= 0){
				destroy();
			}
		}
		
		public void draw(Graphics2D g){
			g.setColor(Color.yellow);
			g.fillArc((int)x-size/2, (int)y-size/2, size, size, 0, 360);
		}
		
	}
	
	
	Player player;
	
	double enemyReloadTime;

	
	public void onStart(){
		player = new Player();
		enemyReloadTime = 0;
		
		// music
		Clip c = Resources.getSound("test.wav");
		c.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	public void update(double dt){
		// fire an enemy missile every so often
		enemyReloadTime -= dt;
		if(enemyReloadTime <= 0){
			
			// create a new missile above top of screen, targeted at player
			double extraDist = 200;
			double x = Utils.randomRange(-extraDist, Game.WIDTH + extraDist);
			new EnemyMissile(x, -50, player.x, player.y);
			
			// wait random amount of time before firing next missile
			enemyReloadTime = Utils.randomRange(0, 3);
		}
	}
	
	public void draw(Graphics2D g){
		// draw crosshairs where our mouse is
		g.setColor(Color.white);
		double ls = 5;
		double targetX = Input.getMouseLoc().x;
		double targetY = Input.getMouseLoc().y;
		g.drawLine((int)(targetX - ls), (int)targetY, (int)(targetX + ls), (int)targetY);
		g.drawLine((int)targetX, (int)(targetY - ls), (int)targetX, (int)(targetY + ls));
	}
	
}
