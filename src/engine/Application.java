package engine;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;

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

		public DrawPanel(){
			// Handle resizing of panel
			addComponentListener(new ComponentAdapter() 
			{  
			        public void componentResized(ComponentEvent evt) {
			        	scaleFac = getHeight() / (double)Game.HEIGHT;
			        	leftSide = (getWidth() - (Game.WIDTH * scaleFac)) / 2;
			        	toBack(); // this seems to (sometimes) fix a bug in Java...
			        	toFront();
			        }
			});
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			draw((Graphics2D) g);
		}
	}

	Game game;
	JPanel panel;
	
	// How to scale and translate the game to fit inside the window
	public static double leftSide = 0;
	public static double scaleFac = 1;

	public Application(String title) {
		// Create Game
		game = new Game();
		
		// Preload resources
		Resources.preloadResources();
		
		// Screen dimensions
		Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
		double screenHeight = screenDimensions.height;
		double screenWidth = screenDimensions.width;

		// Create window
		setTitle(title);
		setLayout(new BorderLayout()); // auto resizes panel
		
		// Create draw panel
		panel = new DrawPanel();
		add(panel, BorderLayout.CENTER);
	
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		
		
		// Fit on screen
		double desiredHeight = screenHeight * .8;
		
		double scale = desiredHeight/(Game.HEIGHT);
		setGameSize(Game.WIDTH * scale, Game.HEIGHT * scale);
		
		setLocation((int)(screenWidth/2 - getWidth()/2),
				(int)(screenHeight*5.5/12.0 - getHeight()/2));
		
		setVisible(true);

		// Add input listeners
		InputListener inputListener = new Input.InputListener();
		addKeyListener(inputListener);
		panel.addMouseListener(inputListener);
		panel.addMouseMotionListener(inputListener);

	}
	
	void setGameSize(double w, double h){
		Insets insets = getInsets();
		int vertInsets = insets.top + insets.bottom;
		int horizInsets = insets.left + insets.right;
		int width = (int)(w + horizInsets);
		int height = (int)(h + vertInsets);
		setSize(width, height);
		setPreferredSize(new Dimension(width, height));
		pack();
		System.out.println("Setting window size: " + width + " " + height);

	}

	// Called about 60 times a second
	// dt is the amount of time (in seconds) passed since last frame
	void update(double dt) {
		game.update(dt);
	}

	void draw(Graphics2D g) {

		AffineTransform prevTransform = g.getTransform();
		
		// Scale to fit window
		g.translate(leftSide, 0);
		g.scale(scaleFac, scaleFac);
		
		game.draw(g);
		
		g.setTransform(prevTransform);
		
		if(leftSide > 0){
			// Draw vertical bars on left and right
			g.setColor(Color.darkGray);
			g.fillRect(0, 0, (int)leftSide+1, getHeight());
			g.fillRect(getWidth()-(int)leftSide-1, 0, (int)leftSide+1, getHeight());			
		}
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
