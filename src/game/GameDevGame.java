package game;

import javax.sound.sampled.Clip;

import engine.*;

public class GameDevGame extends GameObject {

	// Starts the game!
	public static void main(String[] args) {
		Application.launch("GameDevGame");
		new GameDevGame();
	}

	// Global variables - it's likely these will be moved later
	
	public static Player player;
	public static Map map;
	
	
	private Camera camera;
	
	public void onStart() {
		// Play some music
		Clip c = Resources.getSound("test.wav");
		c.loop(Clip.LOOP_CONTINUOUSLY);
//		c.stop();
		
		camera = new Camera();
		new Background("background.png");
		
		// Load level from file
		map = new Map("TestLevel.txt");
		
		player = new Player();
		
		// Tell camera to follow the player
		camera.setTarget(player);
		
		new HeatMeter();
	}

}
