package game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;

import engine.*;

public class Player extends MapEntity {

	private static final double FALL_DURATION = .75;
	
	private static final double JUMP_DURATION = .36;
	private static final double JUMP_HEIGHT = 21;
	
	private static final double STEP_DURATION = .25;
	private static final double STEP_HEIGHT = 5.5;
	
	private int width = 40, height = 50;

	// This makes the player appeared to be centered at his feet
	private static final double Y_DRAW_OFFSET = 10;

	// Radius of circle that bumps into walls
	private int wallRadius = 20;
	
	// Radius of circle that must be off floor for player to fall
	private int floorRadius = 16;

	// Movement variables
	private double maxV = 260;
	private double acc = 2600;
	private double fric = 10;

	// Velocity
	private double vx, vy;
	
	// Falling
	private boolean falling;
	private double fallTime;
	
	// Jumping
	private boolean jumping;
	private double jumpTime;
	
	// Walking
	private boolean walking;
	private double walkTime; 
	
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
		
		jumping = false;
		jumpTime = 0;
		
		walking = false;
		walkTime = 0;
	}

	public void update(double dt) {
		super.update(dt);
		
		// Falling
		
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
		
		// Walk cycle
		
		if (walking){
			// Walk cycle goes from 0 to STEP_DURATION and wraps around
			walkTime += dt;
			if(walkTime > STEP_DURATION)
				walkTime = 0;
		} else {
			// If not walking, get back to being on the ground (walkTime == 0)
			if(walkTime != 0){
				// Quickest way to get to walkTime == 0
				int dir = (walkTime > STEP_DURATION/2 ? 1 : -1);
				
				walkTime += dt * dir;
				
				// Back on ground
				if(walkTime >= STEP_DURATION || walkTime <= 0)
					walkTime = 0;
			}
		}
		
		// Movement

		walking = false;

		if(!falling){
			// Key input
			if (Input.isDown(KeyEvent.VK_A)){
				vx -= acc * dt;
				walking = true;
			}
			if (Input.isDown(KeyEvent.VK_D)){
				vx += acc * dt;
				walking = true;
			}
			if (Input.isDown(KeyEvent.VK_W)){
				vy -= acc * dt;
				walking = true;
			}
			if (Input.isDown(KeyEvent.VK_S)){
				vy += acc * dt;
				walking = true;
			}
			
			if(!jumping && Input.isPressed(KeyEvent.VK_SPACE)){
				jumping = true;
			}
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
		
		// Jumping
		if(jumping){
			jumpTime += dt;
			if(jumpTime >= JUMP_DURATION){
				jumpTime = 0;
				jumping = false;
			}
			
			walking = false;
		}
		
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
		
		if(!jumping && !map.isOnFloor(x-floorRadius, y-floorRadius, floorRadius*2, floorRadius*2)){
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
		
		boolean mouseFiring = false;
		if(Input.isMouseDown()){
			mouseFiring = true;
		}
		
		// Can't shoot if falling
		if(!falling){
			if(shouldBeFiring){
				double fireAngle = Math.atan2(-yDir, xDir);
				flameThrower.angle = Math.toDegrees(fireAngle);
				flameThrower.setFiring(true);
			} else if (mouseFiring){
				Point mp = Input.getMouseLoc();
				double xAim = mp.x - Game.WIDTH/2;
				double yAim = mp.y - Game.HEIGHT/2 - FlameThrower.Y_OFFS;
				double fireAngle = Math.atan2(-yAim, xAim);
				flameThrower.angle = Math.toDegrees(fireAngle);
				flameThrower.setFiring(true);
			} else {
				flameThrower.setFiring(false);
			}
		}
		
		
		// Press 1 to test getting stuck in a wall
		if (Game.DEBUG && Input.isPressed(KeyEvent.VK_1)) {
			x = 32 + 64 * 5;
			y = 32;
		}
	}

	public void draw(Graphics2D g) {
		AffineTransform prev = g.getTransform();
		
		// We subtract this offset to make (x, y) be the coord of the player's feet, instead of his middle
		g.translate(x, (y - Y_DRAW_OFFSET));
		
		// If falling, scale the sprite down to make it look like we're falling 
		if(falling){
			double fac = 1 - (fallTime / FALL_DURATION);
			g.scale(fac, fac);
		}
		
		// Draw shadow
		g.setColor(new Color(0, 0, 0, 100));
		int shadowHeight = height/4;
		int shadowWidth = width + 8;
		g.fillRect((int) (-shadowWidth / 2), (int) (width/2-shadowHeight/2), shadowWidth, shadowHeight);
		
		// Draw player
		double yo = 0; // y offset
		if(jumping){
			double fx = jumpTime / JUMP_DURATION;
			double fofx = -4 * (fx * fx - fx); // -4x^2 + 4x
			yo = fofx * -JUMP_HEIGHT;
		} else {
			double fx = walkTime / STEP_DURATION;
			double fofx = -4 * (fx * fx - fx); // -4x^2 + 4x
			yo = fofx * -STEP_HEIGHT;
		}
		
		g.setColor(Color.white);
		int left = (int)(-width/2);
		int top = (int) (-width / 2 - (height - width) + yo);
		g.fillRect(left, top, width, height);

		g.setTransform(prev);

		if(Game.DEBUG){
			// Draw collision bounds (for debugging)
			g.setColor(Color.green);
			g.drawArc((int)(x-wallRadius), (int)(y-wallRadius), wallRadius*2,wallRadius*2, 0, 360);
			g.setColor(Color.red);
			g.drawRect((int)(x-floorRadius), (int)(y-floorRadius), floorRadius*2,floorRadius*2);
		}
	}

}
