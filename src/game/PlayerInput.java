package game;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import engine.Game;
import engine.Input;

/**
 * Manages the Player.java's input variables based on actual button presses.
 */
public class PlayerInput {
	
	// Holds all inputs for player 
	private class InputState {
		public double x, y, aim;
		public boolean fire, weapon, jump;
		public double dt;
		
		public InputState(Player p, double dt){
			copyFrom(p);
			this.dt = dt;
		}
		
		void copyFrom(Player player){
			x = player.inputX;
			y = player.inputY;
			aim = player.aimInput;
			fire = player.fireInput;
			jump = player.jumpInput;
			weapon = player.weaponInput;
		}
		
		void copyTo(Player player){
			player.inputX = x;
			player.inputY = y;
			player.aimInput = aim;
			player.fireInput = fire;
			player.weaponInput = weapon;
			player.jumpInput = jump;
		}
	}
	
	private static ArrayList<InputState> prevInputs = new ArrayList<InputState>();
	
	protected void runInputs(Player player, double elapsed){
		
		// Figure out how many frames to "rewind"
		// We should be doing a fixed time-step, but this works for now...
		int startIndex = 0;
		for(int i = prevInputs.size()-1; i >= 0; i--){
			InputState prevInput = prevInputs.get(i);
			elapsed -= prevInput.dt;
			if(elapsed <= 0){
				startIndex = i;
				break;
			}
		}

		// Store current input so we can restore it later
		InputState currInput = new InputState(player, 0);
		
		// The first frame may have a smaller dt
		InputState firstInput = prevInputs.get(startIndex);
		firstInput.copyTo(player);
		player.update(firstInput.dt + elapsed, true); // factor in remaining time
		
		// Run the needed frames
		for(int i = startIndex+1; i < prevInputs.size(); i++){
			InputState prevInput = prevInputs.get(i);
			
			prevInput.copyTo(player);
			
			player.update(prevInput.dt, true);
		}
		
		// restore curr input
		currInput.copyTo(player);
	}
	
	protected void updatePlayer(Player player, double dt){
		
		// Movement 
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
		player.inputX = attemptInputX;
		player.inputY = attemptInputY;
		
		
		// Jumping
		
		if(Input.isPressed(KeyEvent.VK_SPACE) && !player.isJumping()){
			player.jumpInput = true;
		} else {
			player.jumpInput = false;
		}
		
		// Aiming/Firing

		Point mp = Input.getMouseLoc();
		double xAim = mp.x - Game.WIDTH/2;
		double yAim = mp.y - Game.HEIGHT/2 - FlameThrower.Y_OFFS;
		player.aimInput = Math.toDegrees(Math.atan2(-yAim, xAim));
		
		if(Input.isMouseDown() && !Input.isRightMouseDown()){
			player.fireInput = true;
		} else {
			player.fireInput = false;
		}
		
		// Weapon
		
		if(Input.isRightMouseDown()){
			player.weaponInput = true;
		} else {
			player.weaponInput = false;
		}
		
		// Store current inputs to replay later
		InputState state = new InputState(player, dt);
		prevInputs.add(state);
		
		//TODO not inf size
	}
	
}
