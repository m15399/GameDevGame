package game;

import java.awt.Color;
import java.awt.Graphics2D;

import engine.Input;
import game.CollisionManager.Collider;
import utils.Utils;

public class BowAndArrow extends Weapon {

	private static final double CHARGE_RATE = .5;
	
	// How much to slow player while charging
	private static final double SLOW_PCT = 40;
	
	
	private double charge = 0;
	
	public BowAndArrow(Player p){
		super(p);
	}
	
	public void update(double dt){
		if(player == Globals.player){
			if(Input.isRightMouseDown()){
				// Charge up while right mouse down
				charge = Utils.clamp(charge + CHARGE_RATE * dt, 0, 1);
				player.setSpeedMul(1 - SLOW_PCT/100);
				
			} else {
				
				double minCharge = .25;
				if(charge > minCharge){
					// fire
					new Arrow(player.x, player.y, player.getAimAngle(), (charge-minCharge)/(1-minCharge));
				}
				
				// If we charged at all, we set player's speed 
				if(charge != 0){
					player.setSpeedMul(1);					
				}
				
				charge = 0;
			}
			
		}
	}
	
	public double getChargePct() {
		return charge;
	}

	public String getName() {
		return "Bow & Arrow";
	}
	
	
	
	private class Arrow extends MapEntity implements Collider {

		private double angleRad, vx, vy, speed, life;
		
		public Arrow(double x, double y, double angle, double power){			
			this.x = x;
			this.y = y;
						
			// We use -angle because our y axis is flipped (down is positive)
			angleRad = Math.toRadians(-angle);
			vx = Math.cos(angleRad);
			vy = Math.sin(angleRad);
			
			double maxPower = 600;
			double minPower = 250;
			
			speed = power * (maxPower-minPower) + minPower;
			life = .8;
			
			fly(30);
		}
		
		public boolean collidesWithPlayer(Player p) {
			return (p.touchesCircle(x, y, 2));
		}

		public void onPlayerCollision(Player p) {
			// TODO - do some damage
			destroy();
		}
		
		private void fly(double dist){
			x += vx * dist;
			y += vy * dist;
		}
		
		public void update(double dt){
			super.update(dt);
			
			life -= dt;
			if(life < 0){
				destroy();
			} else {
				Globals.collisionManager.addColliderForFrame(this);				
			}
			
			fly(speed*dt);
		}
		
		public void draw(Graphics2D g){			
			g.setColor(Color.white);
			Utils.fillRect(g, x, y, 35, 10, angleRad, 1);
		}
	}
	
}
