package game;

import javax.sound.sampled.Clip;

import engine.*;

public class GameDevGame extends GameObject {

	// Starts the game!
	public static void main(String[] args) {
		Application.launch("GameDevGame");
		new GameDevGame();
	}

	Camera camera;
	Map map;
	Player player;
	
	
	public void onStart() {
		// Play some music
		Clip c = Resources.getSound("test.wav");
		c.loop(Clip.LOOP_CONTINUOUSLY);
//		c.stop();
		
		camera = new Camera();
		
		// Load level from file
		map = new Map("TestLevel.txt");
		
		player = new Player();
		player.map = map;
		
		// Tell camera to follow the player
		camera.setTarget(player);
		
	}

}
