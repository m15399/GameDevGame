package game;

import javax.sound.sampled.Clip;

import engine.*;

public class GameDevGame extends GameObject {

	// Starts the game!
	public static void main(String[] args) {
		Application.launch("GameDevGame");
		new GameDevGame();
	}

	public void onStart() {
		// Play some music
		Clip c = Resources.getSound("test.wav");
		c.loop(Clip.LOOP_CONTINUOUSLY);
		
		new TestRectangleGuy();
	}

}
