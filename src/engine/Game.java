package engine;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Makes sure GameObjects are updated and drawn each frame, updates Input,
 * clears screen. 
 */
public class Game {

	public static final int WIDTH = 800, HEIGHT = 600;

	/**
	 * Time passed since start of game
	 */
	public static double time;
	
	public Game() {
		time = 0;
	}

	public void update(double dt) {
		time += dt;
		
		// update all gameobjects
		GameObject.updateAll(dt);

		// update Input
		Input.update();
	}

	public void draw(Graphics2D g) {
		// clear screen
		g.setColor(Color.black);
		g.fillRect(0, 0, WIDTH, HEIGHT);

		// draw all gameobjects
		GameObject.drawAll(g);
	}
}
