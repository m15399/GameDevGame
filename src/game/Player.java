package game;

import java.awt.*;
import java.awt.geom.AffineTransform;

import server.Server;
import utils.Utils;

import network.Client;
import network.message.PlayerInputMessage;
import network.message.PlayerUpdateMessage;

import engine.*;
import game.weapon.Weapon;

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
	
	private static final double FAKE_POS_INTERP_SPEED = 2; 
	
	private int width = 40, height = 50;
	
	private String name;

	// This makes the player appeared to be centered at his feet
	private static final double Y_DRAW_OFFSET = 10;

	// Radius of circle that bumps into walls
	private int wallRadius = 20;
	
	// Radius of circle that must be off floor for player to fall
	private int floorRadius = 17;
	
	// How far away do projectile hit boxes have to be to hit you
	private int projectileRadius = 20;

	// Movement variables
	private double acc = 2600;
	private double fric = 10;
	private double speedMul = 1;
	
	// Input
	private PlayerInput playerInput = null;
	protected double inputX, inputY, aimInput;
	protected boolean fireInput, weaponInput, jumpInput;

	// FakePos - we use this to make the player interpolate to adjusted positions
	// rather than popping immediately
	private double fpx, fpy;
	
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
	private double aimAngle;
	private FlameThrower flameThrower;
	
	// Weapon
	private Weapon currWeapon;
	
	// Network info
	private short playerNumber;
	private boolean dummy;
	private PlayerUpdateMessage lastUpdateMessage;
	private PlayerInputMessage lastInputMessage;
	private double lastUpdateMessageTime;
	
	
	public Player(short playerNumber){
				
		flameThrower = new FlameThrower(this);
		setWeapon(null);
		
		dummy = false;
		this.playerNumber = playerNumber;
				
		playerInput = new PlayerInput();
		
		firing = false;
		aimAngle = 0;
		
		lastUpdateMessage = null;
		
		setName("");
		
		Globals.playerManager.addPlayer(this);
	}
	
	public void setWeapon(Weapon wep){
		if(currWeapon != null)
			currWeapon.destroy();
		currWeapon = wep;
	}
	
	public void setName(String theName){
		if(theName.length() == 0 && playerNumber != -1){
			name = "Player " + playerNumber;
		} else {
			name = theName;			
		}
	}
	
	public String getName(){
		return name;
	}
	
	public short getPlayerNumber(){
		return playerNumber;
	}
	
	public void makeDummy(){
		dummy = true;
		playerInput = null;
	}
	
	public boolean isDummy(){
		return dummy;
	}
	
	public boolean isJumping(){
		return jumping;
	}
	
	public boolean isFalling(){
		return falling;
	}
	
	public void setSpeedMul(double mul){
		speedMul = mul;
	}
	
	/**
	 * Returns where we're aiming (in degrees)
	 */
	public double getAimAngle(){
		return aimAngle;
	}
	
	public double[] getCameraPos(){
		return new double[]{fpx, fpy};
	}
	
	/**
	 * Returns true if the circle would be touching this player
	 */
	public boolean touchesCircle(double cx, double cy, double cr){
		double dx = cx - x;
		double dy = cy - y;
		double dist = Math.sqrt(dx * dx + dy * dy);
		if(dist < cr + projectileRadius)
			return true;
		else
			return false;
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
		
		setWeapon(null);
		
		walking = false;
		walkTime = 0;
		
		syncFakePosition();
	}
	
	public FlameThrower getFlameThrower(){
		return flameThrower;
	}
	
	public Weapon getCurrWeapon(){
		return currWeapon;
	}
	
	private void syncFakePosition(){
		fpx = x;
		fpy = y;
	}
	
	private void sendInputsToServer(){
		if(dummy)
			return;
		
		PlayerInputMessage currMessage = new PlayerInputMessage(inputX, inputY, aimInput, 
				fireInput, weaponInput, jumpInput);
		
		// Should we send an update to the server right now?
		boolean shouldUpdate = (lastUpdateMessage == null || // first update
				Game.getTime() - lastUpdateMessageTime > .5 || // has not updated recently
				lastInputMessage.fire != currMessage.fire || // state changes...
				lastInputMessage.jump != currMessage.jump ||
				lastInputMessage.weapon != currMessage.weapon ||
				lastInputMessage.x != currMessage.x ||
				lastInputMessage.y != currMessage.y ||
				Math.abs(Utils.angleDifference(lastInputMessage.aim, aimInput)) > 15);
		
		// Update the server
		if(shouldUpdate){
			
			// Send our name every so often (not every update though)
			// TODO - put this back in
//			if(Math.random() < .2){
//				currMessage.name = name;
//			}
			
			Client.sendMessage(currMessage);
			lastInputMessage = currMessage;
		}
	}
	
	public void receieveInputs(PlayerInputMessage inputs){
		inputX = inputs.x;
		inputY = inputs.y;
		aimInput = inputs.aim;
		fireInput = inputs.fire;
		jumpInput = inputs.jump;
		weaponInput = inputs.weapon;
	}
	
	/**
	 * Update the server about our current state
	 */
	private void sendUpdateFromServerToClients(){

		if(!Globals.isServer()) // 
			return;
		
		PlayerUpdateMessage currMessage = new PlayerUpdateMessage(playerNumber, x, y, vx, vy, 
				inputX, inputY, aimAngle, jumpTime, fallTime, firing && !flameThrower.isOverheating(), 
				jumping, falling);
		
		// Should we send an update right now?
		boolean shouldUpdate = (lastUpdateMessage == null || // first update
				Game.getTime() - lastUpdateMessageTime > .25 || // has not updated recently
				lastUpdateMessage.falling != currMessage.falling || // state changes...
				lastUpdateMessage.firing != currMessage.firing ||
				lastUpdateMessage.jumping != currMessage.jumping ||
				lastUpdateMessage.inputX != currMessage.inputX ||
				lastUpdateMessage.inputY != currMessage.inputY ||
				Math.abs(Utils.angleDifference(lastUpdateMessage.angle, aimAngle)) > 10);
		

		if(shouldUpdate){
			
			// Send our name every so often (not every update though)
			if(Math.random() < .2){
				currMessage.name = name;
			}
			
			currMessage.time = Globals.getNetworkGameTime();
			
			Server.forwardToAll(currMessage);
			
			lastUpdateMessage = currMessage;
			lastUpdateMessageTime = Game.getTime();
		}
	}

	/**
	 * Update this Player object to match the state we recieved from the server.
	 */
	public void recieveUpdateFromServer(PlayerUpdateMessage msg){
		
		x = msg.x;
		y = msg.y;
		vx = msg.vx;
		vy = msg.vy;
		if(dummy){
			inputX = msg.inputX;
			inputY = msg.inputY;
			aimInput = msg.angle;
			fireInput = msg.firing;			
		}
		
		setFalling(msg.falling);
		setJumping(msg.jumping);
		fallTime = msg.fallTime;
		jumpTime = msg.jumpTime;
		
		if(msg.name.length() > 0){
			setName(msg.name);
		}
		
		/*
		
		This is a common technique in online games - what we're going to do
		when we receive a state update about the local player is first update
		the local player to match the server's state. Then we're going to re-
		simulate any inputs that happened since the update was sent. If lag
		isn't too bad, the player won't notice that his "real" (authoritative)
		movement is happening somewhere else. 
		
		*/
		if(playerInput != null){
			double timeDif = 2 * (Globals.getNetworkGameTime() - msg.time);
			playerInput.runInputs(this, timeDif);
		}
		
	}
	
	/**
	 * "Rewinds" happen when we recieve an update about our position
	 * from the server and need to replay recent inputs. While rewinding,
	 * we don't want to modify certain things - mainly modify our position.
	 */
	public void update(double dt, boolean isRewind){
		if(playerInput != null && !isRewind){
			playerInput.updatePlayer(this, dt);
			sendInputsToServer();
		}
		
		updateMovement(dt, isRewind);
		checkCollisions(dt, isRewind);
		updateJumping(dt);
		updateFalling(dt);
		
		if(!isRewind){
			updateWalkAnimation(dt);
			sendUpdateFromServerToClients();
			updateFlameThrower(dt);
			updateWeapon(dt);
		}
	}
	
	public void update(double dt) {
		super.update(dt);
		
		update(dt, false);
	}
	
	private void setFalling(boolean f){
		if(falling && !f){
			// stop falling
			falling = false;
			fallTime = 0;
			setDrawOrder(0);
			syncFakePosition();
		} else if (!falling && f){
			// start falling
			falling = true;
			fallTime = 0;
			setDrawOrder(0);
			
			if(!dummy)
				Sound.playEffect("fall.wav");
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
				if(Globals.isAuthoritative())
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
	
	private void updateMovement(double dt, boolean isRewind){
		
		walking = false;

		if(!falling){
			// At this point inputX and inputY are either:
			// 1. Player input, if we're the local player
			// 2. What the server told us, if we're a dummy player

			double moveX = inputX * speedMul;
			double moveY = inputY * speedMul;
			
			if(moveX != 0 || moveY != 0){
				walking = true;
				vx += acc * moveX * dt;
				vy += acc * moveY * dt;
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
		
		if(!isRewind){
			fpx += vx * dt;
			fpy += vy * dt;			
		}

		// Friction
		vx *= 1 - (dt * fric);
		vy *= 1 - (dt * fric);
		
		
		// Fake position
		if(!isRewind){
			double dfpx = x - fpx;
			double dfpy = y - fpy;
			
			double tolerance = 1;
			
			if(Math.abs(dfpx) < tolerance){
				fpx = x;
			}
			if(Math.abs(dfpy) < tolerance){
				fpy = y;
			}
			
			fpx += dfpx * FAKE_POS_INTERP_SPEED * dt;
			fpy += dfpy * FAKE_POS_INTERP_SPEED * dt;
		}
		
		
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
			
			if(!dummy)
				Sound.playEffect("jump.wav");
		}
	}
	
	private void updateJumping(double dt){
		
		if(!falling && !jumping && jumpInput){
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

	private void checkCollisions(double dt, boolean isRewind){
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
		
		if(!isRewind){
			fpx += pushX;
			fpy += pushY;			
		}

		// Collision with floor
		if(!jumping){
			if(!map.isOnFloor(x-floorRadius, y-floorRadius, floorRadius*2, floorRadius*2))
				setFalling(true);
		}
	}
	
	private void updateFlameThrower(double dt){
		
		firing = fireInput;
		aimAngle = aimInput;
		
		// Can't shoot if falling
		if(firing && !falling){			
			flameThrower.angle = aimAngle;
			flameThrower.setFiring(true);
		} else {
			flameThrower.setFiring(false);
		}
	}
	
	private void updateWeapon(double dt){
		if(currWeapon != null){
			if(falling){
				currWeapon.setInput(false);
			} else {
				currWeapon.setInput(weaponInput);
			}
		}
	}

	public void draw(Graphics2D g) {
		AffineTransform prev = g.getTransform();
		
		// We subtract this offset to make (x, y) be the coord of the player's feet, instead of his middle
		// We use the "fake pos" - the position that looks correct but isn't quite
		g.translate(fpx, (fpy - Y_DRAW_OFFSET));
		
		// If falling, scale the sprite down to make it look like we're falling 
		if(falling){
			double fac = 1 - (fallTime / FALL_DURATION);
			g.scale(fac, fac);
		}
		
		// Draw shadow
		g.setColor(new Color(0, 0, 0, 100));
		int shadowHeight = height/4;
		int shadowWidth = width + 0; // 8
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
		
		// Draw a white box
//		g.setColor(Color.white);
//		int left = (int)(-width/2);
//		int top = (int) (-width / 2 - (height - width) + yo);
//		g.fillRect(left, top, width, height);
		
		// Draw player sprite
		Utils.drawImage(g, "player.png", 0, yo-16, 0, .5);
		
		g.setColor(Color.white);
		g.setFont( new Font("Arial", Font.BOLD, 12) );
		Utils.drawStringCentered(g, name, 0, -height / 2 - 16);
		
		g.setTransform(prev);
		
		if(Globals.DEV_MODE){
			// Real pos
			g.setColor(Color.white);
			g.fillRect((int)x, (int)y, 3, 3);
			
			// Draw collision bounds (for debugging)
			g.setColor(Color.green);
			g.drawArc((int)(x-wallRadius), (int)(y-wallRadius), wallRadius*2,wallRadius*2, 0, 360);
			g.setColor(Color.red);
			g.drawRect((int)(x-floorRadius), (int)(y-floorRadius), floorRadius*2,floorRadius*2);
		}
	}

}
