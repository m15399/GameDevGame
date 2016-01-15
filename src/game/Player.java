package game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;

import network.Client;
import network.PlayerUpdateMessage;

import engine.*;

/**
 * Controls the player behavior. Note that it has two modes - regular and dummy mode.
 * Dummy mode is akin to a "dumb terminal". In dummy mode, the Player just displays the
 * state of the player that's being sent by the server, and skips a lot of the normal
 * calculations that happen to regular players. 
 */
public class Player extends MapEntity {

	private static final double FALL_DURATION = .75;
	
	private static final double JUMP_DURATION = .36;
	private static final double JUMP_HEIGHT = 21;
	
	private static final double STEP_DURATION = .25;
	private static final double STEP_HEIGHT = 5.5;
	
	private static final double MAX_VELOCITY = 260;
	
	private int width = 40, height = 50;

	// This makes the player appeared to be centered at his feet
	private static final double Y_DRAW_OFFSET = 10;

	// Radius of circle that bumps into walls
	private int wallRadius = 20;
	
	// Radius of circle that must be off floor for player to fall
	private int floorRadius = 17;

	// Movement variables
	private double acc = 2600;
	private double fric = 10;
	
	// Move inputs
	private double inputX, inputY;

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
	
	// Flamthrower
	private boolean firing;
	private double flameAngle;
	private FlameThrower flameThrower;
	
	// Network info
	private short playerNumber;
	private boolean dummy;
	private PlayerUpdateMessage lastMessage;
	private double lastMessageTime;
	
	
	public Player(short playerNumber){
		flameThrower = new FlameThrower(this);
		dummy = false;
		this.playerNumber = playerNumber;
		
		firing = false;
		flameAngle = 0;
		
		lastMessage = null;
		
		Globals.playerManager.addPlayer(this);
	}
	
	public short getPlayerNumber(){
		return playerNumber;
	}
	
	public void makeDummy(){
		dummy = true;
	}
	
	public boolean isDummy(){
		return dummy;
	}
	
	public void onStart() {
		respawn();
	}

	public void onDestroy(){
		Globals.playerManager.removePlayer(this);
	}
	
	public void respawn(){
		x = 100;
		y = 100;
		
		setFalling(false);
		
		setJumping(false);
		
		walking = false;
		walkTime = 0;
	}
	
	public FlameThrower getFlameThrower(){
		return flameThrower;
	}
	
	/**
	 * Update the server about our current state
	 */
	private void sendUpdateToServer(){
		// Dummy's don't update server
		if(dummy)
			return; 
		
		PlayerUpdateMessage currMessage = new PlayerUpdateMessage(playerNumber, x, y, vx, vy, 
				inputX, inputY, flameAngle, firing && !flameThrower.isOverheating(), 
				jumping, falling);
		
		// Should we send an update to the server right now?
		boolean shouldUpdate = (lastMessage == null || // first update
				Game.time - lastMessageTime > .5 || // has not updated recently
				lastMessage.falling != currMessage.falling || // state changes...
				lastMessage.firing != currMessage.firing ||
				lastMessage.jumping != currMessage.jumping ||
				lastMessage.inputX != currMessage.inputX ||
				lastMessage.inputY != currMessage.inputY ||
				Math.abs(Utils.angleDifference(lastMessage.angle, flameAngle)) > 15);
		
		// Update the server
		if(shouldUpdate){
			Client.sendMessage(currMessage);
			lastMessage = currMessage;
			lastMessageTime = Game.time;
		}
	}

	/**
	 * Update this Player object to match the state we recieved from the server.
	 */
	public void recieveUpdateFromServer(PlayerUpdateMessage msg){
		if(!isDummy()){
			Utils.err("Recieved update about non-dummy player. Something's wrong here...");
		}
		
		x = msg.x;
		y = msg.y;
		vx = msg.vx;
		vy = msg.vy;
		inputX = msg.inputX;
		inputY = msg.inputY;
		flameAngle = msg.angle;
		firing = msg.firing;
		
		setFalling(msg.falling);
		setJumping(msg.jumping);
	}
	
	public void update(double dt) {
		super.update(dt);
		
		updateFalling(dt);
		updateWalkAnimation(dt);
		updateMovement(dt);
		updateJumping(dt);
		checkCollisions(dt);
		updateFlameThrower(dt);
		
		
		// Press 1 to test getting stuck in a wall
		if (Game.DEBUG && Input.isPressed(KeyEvent.VK_1)) {
			x = 32 + 64 * 5;
			y = 32;
		}
		
		sendUpdateToServer();
	}
	
	private void setFalling(boolean f){
		if(falling && !f){
			// stop falling
			falling = false;
			fallTime = 0;
			setDrawOrder(0);
		} else if (!falling && f){
			// start falling
			falling = true;
			fallTime = 0;
			setDrawOrder(0);
		}
	}
	
	private void updateFalling(double dt){
		if(falling){
			fallTime += dt;
			
			// Now fallen below the ground layer
			if(fallTime >= FALL_DURATION/3){
				setDrawOrder(-1);
			} 
			
			// Done falling
			if(fallTime >= FALL_DURATION){
				if(!dummy)
					respawn();
				else
					fallTime = FALL_DURATION;
			}
		} 
	}
	
	private void updateWalkAnimation(double dt){
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
	}
	
	private void updateMovement(double dt){
		walking = false;

		if(!falling){
			
			// Key input, if applicable
			if(!dummy){
				double attemptInputX = 0;
				double attemptInputY = 0;
				
				if (Input.isDown(KeyEvent.VK_A)){
					attemptInputX += -1;
				}
				if (Input.isDown(KeyEvent.VK_D)){
					attemptInputX += 1;
				}
				if (Input.isDown(KeyEvent.VK_W)){
					attemptInputY += -1;
				}
				if (Input.isDown(KeyEvent.VK_S)){
					attemptInputY += 1;
				}
				
				// Update our inputX and inputY
				inputX = attemptInputX;
				inputY = attemptInputY;
				
			}
			
			// At this point inputX and inputY are either:
			// 1. Player input, if we're the local player
			// 2. What the server told us, if we're a dummy player
			
			if(inputX != 0 || inputY != 0){
				walking = true;
				vx += acc * inputX * dt;
				vy += acc * inputY * dt;
			}
			
		} 

		// Cap velocity
		double cv = Math.sqrt(vx * vx + vy * vy);
		if (cv > MAX_VELOCITY) {
			double fac = MAX_VELOCITY / cv;
			vx *= fac;
			vy *= fac;
		}
		
		// Move player
		x += vx * dt;
		y += vy * dt;

		// Friction
		vx *= 1 - (dt * fric);
		vy *= 1 - (dt * fric);
	}
	
	private void setJumping(boolean j){
		if(jumping && !j){
			// stop jumping
			jumping = false;
			jumpTime = 0;
		}else if(!jumping && j){
			// start jumping
			jumping = true;
			jumpTime = 0;
		}
	}
	
	private void updateJumping(double dt){
		
		boolean wantsToJump = false;
		
		if(!dummy && Input.isPressed(KeyEvent.VK_SPACE)){
			wantsToJump = true;
		}
		
		if(!falling && !jumping && wantsToJump){
			setJumping(true);
		}
		
		if(jumping){
			jumpTime += dt;
			if(jumpTime >= JUMP_DURATION){
				setJumping(false);
			}
			
			walking = false;
		} 
	}

	private void checkCollisions(double dt){
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

		Map map = Globals.map;
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
			vx = Utils.clamp(vx, 0, MAX_VELOCITY);
		}

		if (rpInsideTile) {
			double dist = map.distIntoTile(x + wallRadius);
			pushX -= Utils.clamp(dist, -maxPush, maxPush);
			vx = Utils.clamp(vx, -MAX_VELOCITY, 0);
		}
		if (tpInsideTile) {
			double dist = map.distLeftInTile(y - wallRadius);
			pushY += Utils.clamp(dist, -maxPush, maxPush);
			vy = Utils.clamp(vy, 0, MAX_VELOCITY);
		}
		if (bpInsideTile) {
			double dist = map.distIntoTile(y + wallRadius);
			pushY -= Utils.clamp(dist, -maxPush, maxPush);
			vy = Utils.clamp(vy, -MAX_VELOCITY, 0);
		}

		x += pushX;
		y += pushY;

		// Collision with floor
		if(!dummy && !jumping){
			if(!map.isOnFloor(x-floorRadius, y-floorRadius, floorRadius*2, floorRadius*2))
				setFalling(true);
		}
	}
	
	private void updateFlameThrower(double dt){
		
		if(!dummy){
			
			// Take input from the player to update firing and flameAngle
			
			firing = false;
			
			int xDir = 0;
			int yDir = 0;
			
			if(Input.isDown(KeyEvent.VK_LEFT)){
				xDir = -1;
			}
			if (Input.isDown(KeyEvent.VK_RIGHT)){
				xDir = 1;
			}
			if(Input.isDown(KeyEvent.VK_UP)){
				yDir = -1;
			}
			if (Input.isDown(KeyEvent.VK_DOWN)){
				yDir = 1;
			}

			if(xDir != 0 || yDir != 0){
				firing = true;
				flameAngle = Math.toDegrees(Math.atan2(-yDir, xDir));
			}
			
			if(Input.isMouseDown()){
				firing = true;
				Point mp = Input.getMouseLoc();
				double xAim = mp.x - Game.WIDTH/2;
				double yAim = mp.y - Game.HEIGHT/2 - FlameThrower.Y_OFFS;
				flameAngle = Math.toDegrees(Math.atan2(-yAim, xAim));
			}
		}
		
		// Can't shoot if falling
		if(firing && !falling){			
			flameThrower.angle = flameAngle;
			flameThrower.setFiring(true);
		} else {
			flameThrower.setFiring(false);
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
