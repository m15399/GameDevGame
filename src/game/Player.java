package game;

import java.awt.*;
import java.awt.event.KeyEvent;

import engine.*;

public class Player extends Entity {

	private int width = 40, height = 50;

	// Radius of circle that bumps into walls
	private int wallRadius = 20;

	// Movement variables
	private double maxV = 260;
	private double acc = 2600;
	private double fric = 10;

	// The map we're on
	Map map;

	// Velocity
	double vx, vy;

	public void onStart() {
		x = 100;
		y = 100;

		drawOrder = 1;
	}

	public void update(double dt) {
		
		
		// Movement

		// Key input
		if (Input.isDown(KeyEvent.VK_LEFT))
			vx -= acc * dt;
		if (Input.isDown(KeyEvent.VK_RIGHT))
			vx += acc * dt;
		if (Input.isDown(KeyEvent.VK_UP))
			vy -= acc * dt;
		if (Input.isDown(KeyEvent.VK_DOWN))
			vy += acc * dt;

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

		boolean lpInsideTile = map.isWallAt(x - wallRadius, y);
		boolean rpInsideTile = map.isWallAt(x + wallRadius, y);
		boolean tpInsideTile = map.isWallAt(x, y - wallRadius);
		boolean bpInsideTile = map.isWallAt(x, y + wallRadius);

		double pushX = 0;
		double pushY = 0;
		double maxPush = 80 * dt;

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

		
		// Press 1 to test getting stuck in a wall
		if (Input.isPressed(KeyEvent.VK_1)) {
			x = 32;
			y = 32;
		}
	}

	public void draw(Graphics2D g) {
		// Draw a rect to represent player
		g.setColor(Color.white);
		g.fillRect((int) (x - width / 2),
				(int) (y - width / 2 - (height - width)), width, height);
		
		// Collision circle (for debugging)
		g.setColor(Color.green);
		// g.drawArc((int)(x-wallRadius), (int)(y-wallRadius), wallRadius*2,
		// wallRadius*2, 0, 360);
	}

}
