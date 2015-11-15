package engine;
import java.awt.*;
import javax.swing.*;

import engine.Input.InputListener;

/**
 * Contains code to create the window, as well as the main game loop. Calls
 * update & draw on the Game about 60 times/sec.
 */
public class Application extends JFrame implements Runnable {

	// need this to stop stupid warnings
	private static final long serialVersionUID = 7625889897152186339L;

	public static void launch(){
		launch("Game"); // default title
	}
	
	public static void launch(String title){
		Application app = new Application(title);
		new Thread(app).start(); // thread.start calls our run method
	}

	
	
	// Panel to draw stuff on
	class DrawPanel extends JPanel {
		
		private static final long serialVersionUID = 1958492344976812306L;

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			draw((Graphics2D) g);
		}
	}

	Game game;

	public Application(String title) {
		// Create Game
		game = new Game();
		
		// Create draw panel
		JPanel panel = new DrawPanel();
		add(panel);
		
		// Add input listeners
		InputListener inputListener = new Input.InputListener();
		addKeyListener(inputListener);
		panel.addMouseListener(inputListener);
		panel.addMouseMotionListener(inputListener);
		
		// Preload resources
		Resources.preloadResources();
		
		// Create window
		setTitle(title);
		setSize(Game.WIDTH, Game.HEIGHT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setResizable(false);
		
		// Resize to take the sides of the window into account
		// (Coordinates will be a little off otherwise)
		Insets insets = getInsets();
		setSize(Game.WIDTH + insets.left + insets.right, 
				Game.HEIGHT + insets.top + insets.bottom);
		
		
	}

	// Called about 60 times a second
	// dt is the amount of time (in seconds) passed since last frame
	void update(double dt) {
		game.update(dt);
	}

	void draw(Graphics2D g) {
		game.draw(g);
	}

	// Game loop -
	// Updates and redraws the window about 60 times a second
	// mostly copied from the internet
	long desiredFPS = 60;
	long desiredDeltaLoop = (1000 * 1000 * 1000) / desiredFPS;

	boolean running = true;

	public void run() {

		long beginLoopTime;
		long endLoopTime;
		long currentUpdateTime = System.nanoTime();
		long lastUpdateTime;
		long deltaLoop;

		while (running) {
			beginLoopTime = System.nanoTime();

			lastUpdateTime = currentUpdateTime;
			currentUpdateTime = System.nanoTime();

			// call update with the time passed
			int millis = (int) ((currentUpdateTime - lastUpdateTime) / (1000 * 1000));
			update(millis / 1000.0);

			repaint(); // repaint the JFrame, calls paintComponent on our panel
			
			endLoopTime = System.nanoTime();
			deltaLoop = endLoopTime - beginLoopTime;

			if (deltaLoop > desiredDeltaLoop) {
				// Do nothing. We are already late.
			} else {
				try {
					Thread.sleep((desiredDeltaLoop - deltaLoop) / (1000 * 1000));
				} catch (InterruptedException e) {
					// Do nothing
				}
			}
		}
	}
}
