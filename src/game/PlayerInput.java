package game;

import java.awt.Point;
import java.awt.event.KeyEvent;

import engine.Game;
import engine.Input;

/**
 * Manages the Player.java's input variables based on actual button presses.
 */
public class PlayerInput {
	
	protected void updatePlayer(Player player){
		
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
		
		if(Input.isPressed(KeyEvent.VK_SPACE)){
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
	}
	
}
