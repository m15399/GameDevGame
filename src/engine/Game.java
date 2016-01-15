package engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;


/**
 * Makes sure GameObjects are updated and drawn each frame, updates Input,
 * clears screen, measures fps. 
 */
public class Game {

	public static final int WIDTH = 800, HEIGHT = 600;
	
	/**
	 * Debug mode?
	 */
	public static boolean DEBUG = false;
	
	/**
	 * Time passed since start of game
	 */
	public static double time;
	public static int frameNumber;
	
	// FPS stuff
	public static double fps;
	private static double framesThisCheck;
	private static double lastFpsCheck;
	private static final double CHECK_FREQ = .25;
	
	
	public Game() {
		time = 0;
		frameNumber = 0;
		
		lastFpsCheck = time;
		fps = 0;
		framesThisCheck = 0;
	}

	private void updateFps(){
		framesThisCheck++;
		if(time > lastFpsCheck + CHECK_FREQ){
			fps = framesThisCheck / CHECK_FREQ;
			framesThisCheck = 0;
			lastFpsCheck += CHECK_FREQ;
		}
	}
	
	public void update(double dt) {
		time += dt;
		frameNumber++;
		
		if(Input.isPressed(KeyEvent.VK_BACK_QUOTE)){
			DEBUG = !DEBUG;
		}
		
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
		
		if(DEBUG){
			g.setColor(Color.white);
			g.drawString(String.format("%d", (int)fps), 10, 20);			
		}
		
		updateFps();
	}
}
