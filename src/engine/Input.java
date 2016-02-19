package engine;

import java.awt.Point;
import java.awt.event.*;
import java.util.HashMap;

import utils.Observer;

/**
 * Keeps track of keyboard and mouse. Can check what keys were pressed this
 * frame and what keys are being held down. Also can check mouse location and
 * whether mouse was pressed this frame/is being held down.
 */
public class Input {

	private static HashMap<Integer, Boolean> keysPressed = new HashMap<Integer, Boolean>();
	private static HashMap<Integer, Boolean> keysDown = new HashMap<Integer, Boolean>();

	private static boolean mousePressed;
	private static boolean mouseDown; 
	private static boolean rightMousePressed;
	private static boolean rightMouseDown;
	private static Point mouseLoc = new Point(0, 0);
	
	private static Observer textInputInterceptor = null; 

	
	//
	// Public methods
	//
	
	/**
	 * User clicked this frame?
	 */
	public static boolean isMousePressed(){
		return mousePressed;
	}
	
	/**
	 * Mouse button currently held down?
	 */
	public static boolean isMouseDown(){
		return mouseDown;
	}
	
	public static boolean isRightMousePressed(){
		return rightMousePressed;
	}
	public static boolean isRightMouseDown(){
		return rightMouseDown;
	}
	
	/**
	 * Where the mouse is right now (in pixel coords)
	 */
	public static Point getMouseLoc(){
		return mouseLoc;
	}
	
	/**
	 * Was the key pressed this frame?
	 */
	public static boolean isPressed(int keyCode) {
		return keysPressed.containsKey(keyCode) && keysPressed.get(keyCode);
	}

	/**
	 * Is the key currently held down?
	 */
	public static boolean isDown(int keyCode) {
		return keysDown.containsKey(keyCode) && keysDown.get(keyCode);
	}
	
	/**
	 * Set an observer to intercept text input (typing). The observer
	 * will be sent a notification every time a character is typed, with the 
	 * character being typed as the argument. While there is an observer
	 * intercepting text input, other forms of key input will not work.
	 */
	public static void interceptTextInput(Observer o){
		textInputInterceptor = o;
	}
	
	/**
	 * Go back to standard key input instead of intercepting it with
	 * an observer. 
	 */
	public static void releaseInterceptTextInput(){
		textInputInterceptor = null;
	}

	public static void update() {
		keysPressed.clear();
		mousePressed = false;
		rightMousePressed = false;
		
		// Stop pasting... (pasting is currently a hack)
		keysDown.put(KeyEvent.VK_PASTE, false);
	}

	//
	// Listener (listens for Java events)
	//
	
	static class InputListener implements MouseListener, MouseMotionListener, KeyListener {
		
		private Point adjustPoint(Point p){
			int x = (int)((p.x - Application.leftSide) / Application.scaleFac);
			int y = (int)(p.y / Application.scaleFac);
			return new Point(x, y);
		}
		
		@Override
		public void keyTyped(KeyEvent e) {
			if(textInputInterceptor != null){
				
				Character c = e.getKeyChar();
				
				// In case we're inputting data in a menu, we don't want to
				// intercept keys like tab
				if (c == '\t'){
					e.setKeyCode(KeyEvent.VK_TAB);
					handlePress(e);
					
				} else {
					textInputInterceptor.notify(c);					
				}
				
			}
		}
		
		private void handlePress(KeyEvent e){			
			if(!isDown(e.getKeyCode()))
				keysPressed.put(e.getKeyCode(), true);
			keysDown.put(e.getKeyCode(), true);	
		}

		@Override
		public void keyPressed(KeyEvent e) {
			
			// For some reason cant detect pasting, so I'm using a hack
			if(e.getKeyCode() == 86){ // v
				if(isDown(17) || isDown(157)){ // ctrl or command
					e.setKeyCode(KeyEvent.VK_PASTE);
				}
			} else if(e.getKeyCode() == KeyEvent.VK_PASTE){
				// Ignore real pastes just in case
				return;
			}
			
			int code = e.getKeyCode();
			
			if(textInputInterceptor == null || code == KeyEvent.VK_PASTE || code == 17 || code == 157){
				handlePress(e);
			}			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			keysDown.put(e.getKeyCode(), false);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			mouseLoc = adjustPoint(e.getPoint());
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			mouseLoc = adjustPoint(e.getPoint());
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON1){
				mousePressed = true;
				mouseDown = true;
			} else if(e.getButton() == MouseEvent.BUTTON3){
				rightMousePressed = true;
				rightMouseDown = true;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON1){
				mouseDown = false;
			} else if(e.getButton() == MouseEvent.BUTTON3){
				rightMouseDown = false;
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}
	}
	
	

}
