package testGames;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import javax.sound.sampled.Clip;

import engine.*;


public class BreakoutTest extends GameObject {

	public static void main(String[] args) {
		Application.launch();
		new BreakoutTest();
	}
	
	
	/**
	 * RectuangularObject is the base class for the objects in the game. 
	 * It lets us get some basic functionality on the bricks, paddle, and ball,
	 * such as drawing a colored rectangle and checking collisions.
	 */
	class RectangularObject extends GameObject {
		double x, y, xs, ys; // location, size
		double xv, yv; // velocity
		Color color;

		public RectangularObject() {
			xs = ys = 10;
			color = Color.white;
		}

		// does it collide with the other rect?
		public boolean collidesWith(RectangularObject other) {
			return Utils.rectsCollide(x, y, xs, ys, other.x, other.y, other.xs,
					other.ys);
		}

		public void update(double dt) {
			// add velocity to position
			x += xv * dt;
			y += yv * dt;
		}

		public void draw(Graphics2D g) {
			g.setColor(color);
			g.fillRect((int) (x - xs / 2), (int) (y - ys / 2), (int) xs,
					(int) ys);
		}
	}
	
	

	class Ball extends RectangularObject {

		// how much the ball speeds up when bouncing off paddle
		static final double SPEEDUP = .03;
		// how much you can effect the angle the ball bounces off paddle
		static final double ANGLE_EFFECT = 50;

		public Ball() {
			// 30x30 pixels
			xs = ys = 30;

			// start in middle of screen
			x = Game.WIDTH / 2;
			y = Game.HEIGHT / 2; 

			// random velocity
			xv = Utils.randomRange(-75, 75);
			yv = 200;
		}

		public void update(double dt) {
			super.update(dt); // need to call super to have parent class's
								// update method runs

			// bounce off walls
			if (x > Game.WIDTH - xs / 2 || x < xs / 2)
				xv *= -1;
			if (y < ys / 2)
				yv *= -1;

		}

		// Overrides RectangularObject.draw 
		public void draw(Graphics2D g) {
			g.drawImage(Resources.getImage("test.png"), (int) x - 21,
					(int) y - 21, null);
		}

		/**
		 * Check for collision with brick.
		 * Returns true if there was a collision
		 */
		public boolean checkCollision(Brick b) {
			if (collidesWith(b)) {
				yv *= -1;
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Check for collision with paddle
		 * Returns true if there was a collision
		 */
		public boolean checkCollision(Paddle p) {
			if (yv > 0 && collidesWith(p)) {
				yv *= -(1 + SPEEDUP);

				// add x velocity based on paddle position
				double dx = p.x - x;
				double max = 150;
				xv = Utils.clamp(xv, -max, max);
				xv -= (dx / (xs / 2)) * ANGLE_EFFECT;

				return true;
			} else {
				return false;
			}
		}
	}

	
	
	class Paddle extends RectangularObject {
		public Paddle() {
			x = Game.WIDTH / 2;
			y = Game.HEIGHT - 50;
			xs = 100;
			ys = 20;
		}

		public void update(double dt) {
			super.update(dt);

			// follow mouse position
			x = Input.getMouseLoc().x;
			x = Utils.clamp(x, xs / 2, Game.WIDTH - xs / 2);
		}
	}

	class Brick extends RectangularObject {
		public Brick(double x, double y) {
			this.x = x;
			this.y = y;
			xs = 50;
			ys = 25;

			tag = "brick";
		}
	}
	
	

	Ball ball;
	Paddle paddle;

	public void onStart(){
		ball = new Ball();
		paddle = new Paddle();

		// create bricks
		for (int i = 0; i < 13 * 4; i++) {
			new Brick(i % 13 * 60 + 40, (i / 13 + 1) * 35);
		}

		// music
		Clip c = Resources.getSound("test.wav");
		c.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	public void update(double dt) {

		ball.checkCollision(paddle);

		// check ball vs brick collisions
		ArrayList<GameObject> bricks = GameObject.findObejctsByTag("brick");
		for (int i = 0; i < bricks.size(); i++) {
			Brick brick = (Brick) bricks.get(i);

			if (ball.checkCollision(brick)) {
				brick.destroy();
				break;
			}
		}
	}

}
