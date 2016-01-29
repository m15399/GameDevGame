package main_menu;

import utils.Observer;
import engine.Game;
import engine.GameObject;
import game.GameDevGame;

/**
 * Main menu for the game (first thing you see)
 */
public class MainMenu extends GameObject {

	// Standard menu button sizes
	public static int BUTTON_WIDTH = 300;
	public static int BUTTON_HEIGHT = 40;
	public static int SPACING = BUTTON_HEIGHT + 14; // vertical space between buttons

	public MainMenu() {

		// Create main menu's buttons
		
		int buttonX = Game.WIDTH / 2;
		int buttonY = Game.HEIGHT / 2 - 50;

		new MenuButton("Join Game", buttonX, buttonY, BUTTON_WIDTH,
				BUTTON_HEIGHT, new Observer() {
					public void notify(Object arg) {
						// Destroy everything and switch to JoinGameMenu
						GameObject.destroyAllObjects();
						new JoinGameMenu();
					}
				});
		buttonY += SPACING;

		new MenuButton("Play Offline", buttonX, buttonY, BUTTON_WIDTH,
				BUTTON_HEIGHT, new Observer() {
					public void notify(Object arg) {
						// Destroy everything and start an online game
						GameObject.destroyAllObjects();
						new GameDevGame();
					}
				});
		buttonY += SPACING;

	}

}
