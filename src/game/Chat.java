package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import utils.Observer;
import utils.Utils;

import network.ChatMessage;
import network.Client;

import engine.Entity;
import engine.Game;
import engine.Input;

/** 
 * Chat display during game
 */
public class Chat extends Entity {

	private static final int FONT_SIZE = 12;
	private static final int SPACING = FONT_SIZE + 4;
	private static final double HEIGHT = 100;
	private static final double WIDTH = 400;
	private static final int MAX_MESSAGE_LENGTH = 64;
	
	private static final String ALLOWED_PUNCT = " !@#$%^&*()-=_+`~[]\\{}|;':\",./<>?";
	
	private ArrayList<String> names;
	private ArrayList<String> messages;
	private int maxMessages;
	
	private double lastChangeTime;
	
	
	private boolean typing; // Player currently typing a message?
	private String currMessage;
	
	public Chat(){
		x = Game.WIDTH/2 - WIDTH/2;
		y = Game.HEIGHT - HEIGHT - 14;
		
		names = new ArrayList<String>();
		messages = new ArrayList<String>();
		
		maxMessages = (int)(HEIGHT/(SPACING));

		lastChangeTime = -100;		
		typing = false;
		
		setDrawOrder(20);

		for(int i = 0; i < maxMessages; i++){
			addMessage("", "");			
		}

		currMessage = "";
		
		if(!Globals.isServer()){
			Globals.publisher().subscribe(ChatMessage.class, new Observer(){
				public void notify(Object arg){
					ChatMessage msg = (ChatMessage)arg;
					addMessage(msg.name, msg.message);
				}
			});
		}
	}
	
	/**
	 * Add a message to the chat box
	 */
	private void addMessage(String name, String msg){
		if(messages.size() >= maxMessages){
			messages.remove(0);
			names.remove(0);
		}
		names.add(name);
		messages.add(msg);
		
		lastChangeTime = Game.time;
	}
	
	/**
	 * Listens for typing while the player is typing a message
	 */
	private class TypingListener implements Observer {
		public void notify(Object arg){
			char ch = (Character) arg;
			
			if(ch == 10){ // Enter
				// Send the message to the server
				if(!currMessage.isEmpty()){
					Client.sendMessage(new ChatMessage(Globals.player.getName(), currMessage));
				}
				stopTypingMode();

			} else if (ch == 27){ // Esc
				// Cancel typing
				stopTypingMode();
				
			} else if (ch == 8){ // Backspace
				// Delete last character
				if(currMessage.length() > 0){
					currMessage = currMessage.substring(0, currMessage.length() - 1);
				}
			}
			
			// Append the character to currMessage if the character is allowed
			if(Character.isLetterOrDigit(ch) || ALLOWED_PUNCT.contains("" + ch)){
				addChar(ch);
			}
			
		}
	}
	
	private void addChar(char ch){
		if(currMessage.length() < MAX_MESSAGE_LENGTH)
			currMessage += ch;
	}
	
	private void addString(String str){
		for(int i = 0; i < str.length(); i++){
			addChar(str.charAt(i));
		}
	}
	
	private void startTypingMode(){
		typing = true;
		Input.interceptTextInput(new TypingListener());
	}

	private void stopTypingMode(){
		currMessage = "";
		typing = false;
		Input.releaseInterceptTextInput();
	}
	
	public void update(double dt){
		if(!typing && Globals.isOnlineClient() && Input.isPressed(KeyEvent.VK_ENTER)){
			startTypingMode();
		}
		
		if(typing && Input.isPressed(KeyEvent.VK_PASTE)){
			addString(Utils.readClipboard());
		}
	}
	
	/**
	 * Returns how the name/message should be printed
	 */
	private String getDisplayString(String name, String msg){
		if(name.isEmpty())
			return "";
		return name + ":   " + msg;
	}
	
	public void draw(Graphics2D g){
		
		// Should the chatbox be drawn?
		boolean visible = typing || Game.time - lastChangeTime < 5;
		if(!visible)
			return;
		
		AffineTransform prev = g.getTransform();
		g.translate(x, y);

		// Draw each message
		g.setColor(new Color(240, 240, 130));
		g.setFont(new Font("Arial", Font.PLAIN, FONT_SIZE));
		for(int i = 0; i < names.size(); i++){
			g.drawString(getDisplayString(names.get(i), messages.get(i)), 0, i * SPACING);
		}
		
		// Draw the current message being typed
		if(typing){
			g.setColor(Color.white);
			g.drawString(getDisplayString(Globals.player.getName(), currMessage), 0, names.size() * SPACING);			
		}
		
		g.setTransform(prev);
	}
	
}
