package main_menu;

import java.awt.event.KeyEvent;

import utils.Observer;
import engine.Game;
import engine.GameObject;
import engine.Input;
import engine.Sound;
import game.GameDevGame;
import game.Globals;

/**
 * Main menu for the game (first thing you see)
 */
public class MainMenu extends GameObject {

	// Standard menu button sizes
	public static int BUTTON_WIDTH = 300;
	public static int BUTTON_HEIGHT = 40;
	public static int SPACING = BUTTON_HEIGHT + 14; // vertical space between buttons

	private TextField nameField;
	private TextField addressField;
	
	public MainMenu() {

		// Create main menu's buttons

		int buttonY = Game.HEIGHT / 2 - 175;
		int buttonX = Game.WIDTH/2;
		int buttonWidth = MainMenu.BUTTON_WIDTH;
		int buttonHeight = MainMenu.BUTTON_HEIGHT;
		int spacing = MainMenu.SPACING;
		
		EnterPressedObserver onEnterPress = new EnterPressedObserver();
		
		new Label("Display Name:", buttonX, buttonY, buttonWidth, buttonHeight);
		buttonY += spacing;
		
		nameField = new TextField(buttonX, buttonY, buttonWidth, buttonHeight);
		nameField.setMaxLength(16);
		nameField.select();
		nameField.setEnterPressedObserver(onEnterPress);
		buttonY += spacing;

		new Label("IP Address (ip:port):", buttonX, buttonY, buttonWidth, buttonHeight);
		buttonY += spacing;
		
		addressField = new TextField(buttonX, buttonY, buttonWidth, buttonHeight);
		addressField.setEnterPressedObserver(onEnterPress);
		buttonY += spacing;
		
		new MenuButton("Join Game", buttonX, buttonY,
				buttonWidth, buttonHeight, new Observer() {
					public void notify(Object arg) {
						join();
					}
				});
		buttonY += spacing;

		buttonY += spacing;
		new MenuButton("Play Offline", buttonX, buttonY, buttonWidth,
				buttonHeight, new Observer() {
					public void notify(Object arg) {
						// Destroy everything and start an online game
						Sound.playEffect("select.wav");
						GameObject.destroyAllObjects();
						new GameDevGame();
					}
				});
		buttonY += spacing;

	}
	
	private class EnterPressedObserver implements Observer {
		public void notify(Object arg) {
			join();
		}		
	}
	
	private void join(){
		Sound.playEffect("select.wav");
		
		String field = addressField.getText();
		int semi = field.indexOf(':');
		if(semi > 0 && semi != field.length()-1){

			// Destroy everything and start the game
			GameObject.destroyAllObjects();
			
			Globals.desiredPlayerName = nameField.getText();

			// Figure out the ip:port entered in the field
			String address = field.substring(0, semi);
			int port = Integer.parseInt(field.substring(semi+1));
			new GameDevGame(address, port);	
		}
	}
	
	public void update(double dt){
		
		if(Input.isPressed(KeyEvent.VK_TAB)){
			if(nameField.isSelected())
				addressField.select();
			else
				nameField.select();
		}
		
		if(Input.isPressed(KeyEvent.VK_ENTER)){
			join();
		}
	}
}
