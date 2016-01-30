package main_menu;

import java.awt.event.KeyEvent;

import utils.Observer;
import engine.Game;
import engine.GameObject;
import engine.Input;
import game.GameDevGame;
import game.Globals;

/**
 * Menu where players join the game
 */
public class JoinGameMenu extends GameObject {

	private TextField nameField;
	private TextField addressField;
	
	public JoinGameMenu(){
		int buttonY = Game.HEIGHT / 2 - 150;
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
		
		new MenuButton("Back", buttonX, buttonY,
				buttonWidth, buttonHeight, new Observer() {
					public void notify(Object arg) {
						GameObject.destroyAllObjects();
						new MainMenu();
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
