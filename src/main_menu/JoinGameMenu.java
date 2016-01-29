package main_menu;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;

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
		
		new Label("Display Name:", buttonX, buttonY, buttonWidth, buttonHeight);
		buttonY += spacing;
		
		nameField = new TextField(buttonX, buttonY, buttonWidth, buttonHeight);
		nameField.setMaxLength(16);
		nameField.select();
		buttonY += spacing;

		new Label("IP Address (ip:port):", buttonX, buttonY, buttonWidth, buttonHeight);
		buttonY += spacing;
		
		addressField = new TextField(buttonX, buttonY, buttonWidth, buttonHeight);
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
	
	private void paste(){
		if(addressField.isSelected()){
			String str = readClipboard();
			if(str != null){
				addressField.appendText(str);
			}
		}
	}
	
	private String readClipboard(){
		String str = null;
		
		try {
			str = (String) Toolkit.getDefaultToolkit()
			        .getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (HeadlessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return str;
	}
	
	public void update(double dt){
		
		if(Input.isPressed(KeyEvent.VK_PASTE)){
			paste();
		}
		
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
