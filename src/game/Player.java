package game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;

import engine.*;

public class Player extends MapEntity {

	private static final double FALL_DURATION = .75;
	
	private int width = 40, height = 50;

	// Radius of circle that bumps into walls
	private int wallRadius = 20;
	
	// Radius of circle that must be off floor for player to fall
	private int floorRadius = 18;

	// Movement variables
	private double maxV = 260;
	private double acc = 2600;
	private double fric = 10;

	// Velocity
	private double vx, vy;
	
	// Falling
	private boolean falling;
	private double fallTime;
	
	private FlameThrower flameThrower;
	
	public Player(){
		flameThrower = new FlameThrower(this);
	}
	
	public void onStart() {
		respawn();
	}
	
	public void respawn(){
		x = 100;
		y = 100;
		setDrawOrder(0);
		
		falling = false;
		fallTime = 0;
	}

	public void update(double dt) {
		super.update(dt);
		
		if(falling){
			fallTime += dt;
			
			// Now fallen below the ground layer
			if(fallTime >= FALL_DURATION/3){
				setDrawOrder(-1);
			}
			
			// Done falling
			if(fallTime >= FALL_DURATION){
				respawn();
			}
		}
		
		// Movement
		
		if(!falling){
			// Key input
			if (Input.isDown(KeyEvent.VK_A))
				vx -= acc * dt;
			if (Input.isDown(KeyEvent.VK_D))
				vx += acc * dt;
			if (Input.isDown(KeyEvent.VK_W))
				vy -= acc * dt;
			if (Input.isDown(KeyEvent.VK_S))
				vy += acc * dt;
		}

		// Cap velocity
		double cv = Math.sqrt(vx * vx + vy * vy);
		if (cv > maxV) {
			double fac = maxV / cv;
			vx *= fac;
			vy *= fac;
		}

		// Move player
		x += vx * dt;
		y += vy * dt;

		// Friction
		vx *= 1 - (dt * fric);
		vy *= 1 - (dt * fric);

		
		// Collisions with walls

		/*
		 * Right now here's what we're doing:
		 * 
		 * Check our farthest Left Point (lp). Is it inside a wall tile? If so,
		 * push us to the right until we're no longer in the tile. Repeat for
		 * the right-most, top-most, and bottom-most points.
		 * 
		 * This is not the greatest solution for collision detection, but it's
		 * pretty simple and works for now
		 */

		Map map = Map.currMap;
		boolean lpInsideTile = map.isWallAt(x - wallRadius, y);
		boolean rpInsideTile = map.isWallAt(x + wallRadius, y);
		boolean tpInsideTile = map.isWallAt(x, y - wallRadius);
		boolean bpInsideTile = map.isWallAt(x, y + wallRadius);

		double pushX = 0;
		double pushY = 0;
		double maxPush = 150 * dt;

		if (lpInsideTile) {
			// How far to push?
			double dist = map.distLeftInTile(x - wallRadius);

			// Push up to maxPush in the correct direction
			pushX += Utils.clamp(dist, -maxPush, maxPush);

			// Cancel any left-ward velocity (since we ran into a wall on our
			// left)
			vx = Utils.clamp(vx, 0, maxV);
		}

		if (rpInsideTile) {
			double dist = map.distIntoTile(x + wallRadius);
			pushX -= Utils.clamp(dist, -maxPush, maxPush);
			vx = Utils.clamp(vx, -maxV, 0);
		}
		if (tpInsideTile) {
			double dist = map.distLeftInTile(y - wallRadius);
			pushY += Utils.clamp(dist, -maxPush, maxPush);
			vy = Utils.clamp(vy, 0, maxV);
		}
		if (bpInsideTile) {
			double dist = map.distIntoTile(y + wallRadius);
			pushY -= Utils.clamp(dist, -maxPush, maxPush);
			vy = Utils.clamp(vy, -maxV, 0);
		}

		x += pushX;
		y += pushY;

		// Collision with floor
		
		if(!map.isOnFloor(x-floorRadius, y-floorRadius, floorRadius*2, floorRadius*2)){
			falling = true;
		}
		
		// Flame thrower
		
		boolean shouldBeFiring = false;
		int xDir = 0;
		int yDir = 0;
		
		if(Input.isDown(KeyEvent.VK_LEFT)){
			shouldBeFiring = true;
			xDir = -1;
		}
		if (Input.isDown(KeyEvent.VK_RIGHT)){
			shouldBeFiring = true;
			xDir = 1;
		}
		if(Input.isDown(KeyEvent.VK_UP)){
			shouldBeFiring = true;
			yDir = -1;
		}
		if (Input.isDown(KeyEvent.VK_DOWN)){
			shouldBeFiring = true;
			yDir = 1;
		}
		
		// Can't shoot if falling
		if(falling)
			shouldBeFiring = false;
		
		if(shouldBeFiring){
			double fireAngle = Math.atan2(-yDir, xDir);
			flameThrower.angle = fireAngle;
			flameThrower.setFiring(true);
		} else {
			flameThrower.setFiring(false);
		}
		
		
		// Press 1 to test getting stuck in a wall
		if (Input.isPressed(KeyEvent.VK_1)) {
			x = 32;
			y = 32;
		}
	}

	public void draw(Graphics2D g) {
		AffineTransform prev = g.getTransform();
		g.translate((int)x, (int)y);
		
		// If falling, scale the sprite down to make it look like we're falling 
		if(falling){
			double fac = 1 - (fallTime / FALL_DURATION);
			g.scale(fac, fac);
		}
		
		// Draw a rect to represent player
		g.setColor(Color.white);
		g.fillRect((int) (-width / 2), (int) (-width / 2 - (height - width)), width, height);
		
		if(Game.DEBUG){
			// Collision circle (for debugging)
			g.setColor(Color.green);
			g.drawArc((int)(-wallRadius), (int)(-wallRadius), wallRadius*2,wallRadius*2, 0, 360);
			g.setColor(Color.red);
			g.drawArc((int)(-floorRadius), (int)(-floorRadius), floorRadius*2,floorRadius*2, 0, 360);
		}
		g.setTransform(prev);
	}

}
