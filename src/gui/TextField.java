package gui;

import java.awt.Color;
import java.awt.event.KeyEvent;

import utils.Observer;
import utils.Utils;

import engine.Input;

/**
 * Text box where player can type stuff. When clicked, it gains focus and intercepts
 * keyboard input. 
 */
public class TextField extends Label {
	
	// Currently selected TextField (will be typed in)
	private static TextField selectedField = null;
	
	private int cursorPos;
	private int maxLength;
	
	Observer enterPressedObserver;
	
	public TextField(double centerX, double centerY, double w, double h){
		// We use a _ character to denote the cursor
		super("_", centerX, centerY, w, h);

		cursorPos = 0;
		maxLength = 64;
		
		enterPressedObserver = null;
		
		setOutlined(true);
	}
	
	public void setEnterPressedObserver(Observer o){
		enterPressedObserver = o;
	}
	
	/**
	 * Set this field to be the selected field. Will intercept
	 * all user keystrokes until deselected.
	 */
	public void select(){
		selectedField = this;
		Input.interceptTextInput(new Observer(){
			public void notify(Object arg){
				Character c = (Character) arg;
				addChar(c);
			}
		});
	}
	
	/**
	 * Unselect this field. Keystrokes will be normal again
	 */
	public void deselect(){
		selectedField = null;
		Input.releaseInterceptTextInput();
	}
	
	/**
	 * Set the maximum number of characters the user can input in this field
	 */
	public void setMaxLength(int len){
		maxLength = len;
	}
	
	public boolean isSelected(){
		return this == selectedField;
	}
	
	/**
	 * Get the typed text. This is different than getContents in that this
	 * method does not include the cursor character.  
	 */
	public String getText(){
		String contents = getContents();
		return contents.substring(0, contents.length()-1);
	}
	
	private void appendText(String s){
		for(int i = 0; i < s.length(); i++){
			addChar(s.charAt(i));
		}
	}
	
	/**
	 * Add a character to the current cursor position. If there's not enough 
	 * space (due to maxLength being exceeded) the character will not be
	 * inserted. 
	 */
	private void addChar(char c){
		String contents = getContents();
		
		if(c == '\n' && enterPressedObserver != null){
			enterPressedObserver.notify(null);
			return;
		}

		if(Character.isLetterOrDigit(c) || c == ' ' || c == ':' || c == '.'){
			if(contents.length() < maxLength){
				setContents(contents.substring(0, cursorPos) + 
						c + contents.substring(cursorPos));
				cursorPos++;				
			}
			
		} else if(c == 8){
			// Delete key
			if(cursorPos > 0){
				setContents(contents.substring(0, cursorPos-1) +
						contents.substring(cursorPos));
				cursorPos--;				
			}
		}
	}
	
	private void paste(){
		String str = Utils.readClipboard();
		if(str != null){
			appendText(str);
		}
	}
	
	public void update(double dt){
		// Select when clicked
		if(isHovered()){
			if(Input.isMousePressed()){
				select();
			}
		}

		if(Input.isPressed(KeyEvent.VK_PASTE) && isSelected()){
			paste();
		}
		
		// Determine color based on selected or not
		int regular = 180;

		if(isSelected())
			setColor(Color.white);
		else 
			setColor(new Color(regular, regular, regular));
	}
	
	public void onDestroy(){
		if(isSelected()){
			deselect();
		}
	}
}
