package game;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import engine.Entity;
import engine.Game;
import engine.GameObject;

/**
 * Controls the camera. You can set a target object for it to follow.
 * 
 * The way it works is a bit wonky, but it's pretty simple: Every GameObject
 * with a "drawOrder" between -10 and 10 will be transformed by the camera. So
 * backgrounds should go below -10, and UI stuff should go above 10.
 * 
 */
public class Camera extends Entity {

	// Target to follow
	Entity target;

	AffineTransform prevTransform;

	public Camera() {
		new CameraTransformStart();
		new CameraTransformEnd();
	}

	/**
	 * Set an Entity to follow
	 */
	public void setTarget(Entity t) {
		target = t;
	}

	/*
	 * These two classes are what do the transforming. This method is a little
	 * hacky, but it's working fine for now.
	 * 
	 * Basically we create an object called CameraTransformStart with a
	 * drawOrder of -10 that transforms the Graphics2D coordinate system when it
	 * gets drawn. So everything drawn after this object will be transformed by
	 * the camera. The CameraTransformEnd object then undoes the camera
	 * transform for objects with drawOrder > 10.
	 */

	class CameraTransformStart extends GameObject {
		public CameraTransformStart() {
			// a little extra to include objects whose drawOrder == 10
			setDrawOrder(-10.00001);
		}

		public void draw(Graphics2D g) {
			// Store the current transform
			prevTransform = g.getTransform();
			
			// Transform to take camera into account
			g.translate(-target.x + Game.WIDTH / 2, -target.y + Game.HEIGHT / 2);
		}
	}

	class CameraTransformEnd extends GameObject {
		public CameraTransformEnd() {
			setDrawOrder(10.00001);
		}

		public void draw(Graphics2D g) {
			// Restore to the transform we had before the camera
			g.setTransform(prevTransform);
		}
	}

}
