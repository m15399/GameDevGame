package game;

import java.util.ArrayList;

import engine.GameObject;

/**
 * Keeps track of all objects trying to collide with stuff. 
 */
public class CollisionManager extends GameObject {

	/**
	 * Collider interface
	 */
	public interface Collider {
		public boolean collidesWithPlayer(Player p);
		public void onPlayerCollision(Player p);
	}
	
	// Colliders for the this frame 
	private ArrayList<Collider> collidersForFrame = new ArrayList<Collider>();
	
	/**
	 * Checks for any collisions between this frame's colliders and the player.
	 * If there are any, onPlayerCollision will be called for that collider.
	 */
	public void checkForPlayerCollisions(Player p){
		if(p.isFalling())
			return;
		
		for(Collider collider : collidersForFrame){
			if(collider.collidesWithPlayer(p)){
				collider.onPlayerCollision(p);
			}
		}
	}
	
	/**
	 * Register a collider to be checked for collisions this frame. Must be 
	 * called every frame if you want to check continuously
	 */
	public void addColliderForFrame(Collider c){
		collidersForFrame.add(c);
	}
	
	public void update(double dt){
		
		// For now, just check for collisions on the local player
		
		Player localPlayer = Globals.player;
		if(localPlayer != null){
			checkForPlayerCollisions(localPlayer);
		}
		
		collidersForFrame.clear();
	}
	
}
