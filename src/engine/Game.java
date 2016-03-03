package engine;

import game.Globals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Date;


/**
 * Makes sure GameObjects are updated and drawn each frame, updates Input,
 * clears screen, measures fps. 
 */
public class Game {

	public static final int WIDTH = 800, HEIGHT = 600;
	
	private static double startTime; // time the game started
	public static int frameNumber;
	
	// FPS stuff
	public static double fps;
	private static double framesThisCheck;
	private static double lastFpsCheck;
	private static final double CHECK_FREQ = .25;
	
	
	public Game() {
		startTime = new Date().getTime();
		frameNumber = 0;
		
		lastFpsCheck = getTime();
		fps = 0;
		framesThisCheck = 0;
	}
	
	/**
	 * Get the time since the game started
	 */
	public static double getTime(){
		return (new Date().getTime() - startTime)/1000.0;
	}

	private void updateFps(){
		framesThisCheck++;
		if(getTime() > lastFpsCheck + CHECK_FREQ){
			fps = framesThisCheck / CHECK_FREQ;
			framesThisCheck = 0;
			lastFpsCheck += CHECK_FREQ;
		}
	}
	
	public void update(double dt) {
		frameNumber++;
		
		if(Input.isPressed(KeyEvent.VK_BACK_QUOTE) && Globals.DEBUG){
			Globals.DEV_MODE = !Globals.DEV_MODE;
		}
		
		// update all gameobjects
		GameObject.updateAll(dt);

		// update Input
		Input.update();
		
		
//		if(Globals.DEBUG){
//			int intTime = (int)Math.floor(Globals.getNetworkGameTime());
//			if(Globals.getNetworkGameTime() - intTime < 1/60.0){
//				System.out.println("TICK " + Globals.getNetworkGameTime());
//			}
//		}
	}

	public void draw(Graphics2D g) {
		// clear screen
		g.setColor(Color.black);
		g.fillRect(0, 0, WIDTH, HEIGHT);

		// draw all gameobjects
		GameObject.drawAll(g);
		
		if(Globals.DEV_MODE){
			g.setColor(Color.white);
			g.drawString(String.format("%d", (int)fps), 10, 20);
		}
		
//		if(Globals.DEBUG){
//			g.setColor(Color.white);
//			g.drawString("" + Globals.getNetworkGameTime(), 200, 100);
//			
//			int intTime = (int)Math.floor(Globals.getNetworkGameTime());
//			if(Globals.getNetworkGameTime() - intTime < 1/30.0){
//				g.fillRect(150, 100, 10, 10);
//			}
//		}
		
		updateFps();
	}
}
