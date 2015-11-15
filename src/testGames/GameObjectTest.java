package testGames;

import java.awt.Graphics2D;
import javax.sound.sampled.Clip;

import engine.*;

/**
 * Simple example of how to use GameObjects
 */
public class GameObjectTest extends GameObject {

	public static void main(String[] args) {
		Application.launch();
		new GameObjectTest();
	}

	private static final double GRAVITY = 100;

	/**
	 * Ball class - ball that is effected by gravity
	 */
	class Ball extends GameObject {
		double x, y; // location
		double xv, yv; // velocity

		double rotation = Utils.randomRange(0, Math.PI * 2);

		// automatically called, because we extend from GameObject
		public void update(double dt) {

			// add our velocity to our position
			x += xv * dt;
			y += yv * dt;

			// add gravity
			yv += GRAVITY * dt;

			// rotate 1 rad/sec
			rotation += 1 * dt;

			// destroy ourself when we get to the bottom of the screen
			if (y > 500)
				destroy();
		}

		// automatically called, because we extend from GameObject
		public void draw(Graphics2D g) {
			Utils.drawImage(g, "test.png", x, y, rotation, 1);
		}
	}

	public void onStart() {
		// music
		Clip c = Resources.getSound("test.wav");
		c.loop(Clip.LOOP_CONTINUOUSLY);
	}

	public void update(double dt) {
		// create a new ball each frame
		Ball ball = new Ball();
		ball.x = Game.WIDTH / 2;
		ball.y = 400;

		// give ball a random velocity
		ball.yv = Utils.randomRange(-250, -150);
		ball.xv = Utils.randomRange(-75, 75);

	}

}
