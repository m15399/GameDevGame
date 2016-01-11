package game;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import engine.Entity;
import engine.Game;
import engine.GameObject;
import engine.Utils;

/**
 * Controls the camera. You can set a target object for it to follow.
 * 
 * The way it works is a bit wonky, but it's pretty simple: Every GameObject
 * with a "drawOrder" between -10 and 10 will be transformed by the camera. So
 * backgrounds should go below -10, and UI stuff should go above 10.
 * 
 */
public class Camera extends GameObject {

	public static Camera currCamera = null;
	
	// Target to follow
	private Entity target;

	private CameraTransformStart cts;
	private CameraTransformEnd cte;
	private AffineTransform prevTransform;

	public Camera() {
		if(currCamera != null){
			Utils.fatal("Trying to instantiate multiple cameras");
		}
		
		cts = new CameraTransformStart();
		cte = new CameraTransformEnd();
		
		target = null;
		
		currCamera = this;
	}

	/**
	 * Set an Entity to follow
	 */
	public void setTarget(Entity t) {
		target = t;
	}
	
	public double[] getPos(){
		double[] pos = new double[2];
		
		if(target != null){
			pos[0] = target.x - Game.WIDTH / 2;
			pos[1] = target.y - Game.HEIGHT / 2;
		} else {
			pos[0] = pos[1] = 0;
		}
		
		return pos;
	}
	
	public void onDestroy(){
		cts.destroy();
		cte.destroy();
		
		currCamera = null;
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

	private class CameraTransformStart extends GameObject {
		public CameraTransformStart() {
			// a little extra to include objects whose drawOrder == 10
			setDrawOrder(-10.00001);
		}

		public void draw(Graphics2D g) {
			// Store the current transform
			prevTransform = g.getTransform();
			
			// Transform to take camera into account
			double[] pos = getPos();
			g.translate(-pos[0], -pos[1]);
		}
	}

	private class CameraTransformEnd extends GameObject {
		public CameraTransformEnd() {
			setDrawOrder(10.00001);
		}

		public void draw(Graphics2D g) {
			// Restore to the transform we had before the camera
			g.setTransform(prevTransform);
		}
	}

}
