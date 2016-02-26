package game.weapon;

import game.Globals;
import game.MapEntity;
import game.Player;
import game.CollisionManager.Collider;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import utils.Utils;

public class Sword extends Weapon {

	private static final double RECHARGE_TIME = .75;
	
	private double charge = 1;
	
	public Sword(Player player) {
		super(player);
	}
	
	public void update(double dt){
		if(input && charge == 1){
			// Fire
			new SwordHitBox(player, player.getAimAngle());
			charge = 0;
		} else {
			// Recharge
			charge = Utils.clamp(charge + 1/RECHARGE_TIME * dt, 0, 1);
		}
	}

	public double getChargePct() {
		return charge;
	}

	public String getName() {
		return "Sword";
	}
	
	private class SwordHitBox extends MapEntity implements Collider {
		
		private static final int SWORD_DIAMETER = 160;
		private static final int SWORD_ARC = 90;
		
		private double angle;
		private double life = .12;
		
		private Player player;
		
		private ArrayList<Player> playersHit = new ArrayList<Player>();
		
		public SwordHitBox(Player p, double angle){
			this.player = p;
			this.angle = angle;
		}
		
		public void update(double dt){
			super.update(dt);
			
			// Follow player
			x = player.x;
			y = player.y-10;
			
			life -= dt;
			if(life < 0)
				destroy();
			else{
				Globals.collisionManager.addColliderForFrame(this);
			}
		}
		
		public void draw(Graphics2D g){
			int arcAngle = SWORD_ARC;
			int size = SWORD_DIAMETER;
			
			g.setColor(Color.cyan);
			g.fillArc((int)x-size/2, (int)y-size/2, size, size, 
					(int)(angle-arcAngle/2), arcAngle);
		}

		public boolean collidesWithPlayer(Player p) {
			
			// Don't hit ourself, don't hit other players multiple times
			if(p == player || playersHit.contains(p))
				return false;
			
			// Figure out if our angle is pointing at the player
			double dx = p.x - x;
			double dy = p.y - y;
			double pAngle = Math.toDegrees(Math.atan2(-dy, dx));
			double angleDif = Utils.angleDifference(pAngle, angle);
			
			if(Math.abs(angleDif) < SWORD_ARC/2 && p.touchesCircle(x, y, SWORD_DIAMETER/2)){
				// Hit!
				playersHit.add(p);
				return true;
			} else {
				return false;
			}			
		}

		public void onPlayerCollision(Player p) {
			// TODO 
		}
		
	}
	
}
