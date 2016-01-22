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
public class Application implements Runnable {

	// How big, in relation to the screen, should the window be when the game starts?
	// (Don't make it much bigger than .8, might make the window bigger than the screen)
	// This value can be overridden in Application.launch method
	public static final double DESIRED_HEIGHT_OF_WINDOW = .8;
	
	public static void launch(){
		launch("Game"); // default title
	}
	
	public static void launch(String title){
		launch(title, true);
	}
	
	public static void launch(String title, boolean launchGui){
		launch(title, launchGui, DESIRED_HEIGHT_OF_WINDOW);
	}
	
	public static void launch(String title, boolean launchGui, double desiredHeightOfWindow){
		Application app = new Application(title, launchGui, desiredHeightOfWindow);
		new Thread(app).start(); // thread.start calls our run method
	}
	
	// Panel to draw stuff on
	private class DrawPanel extends JPanel {
		
		private static final long serialVersionUID = 1958492344976812306L;

		public DrawPanel(){
			// Handle resizing of panel
			addComponentListener(new ComponentAdapter() 
			{  
			        public void componentResized(ComponentEvent evt) {
			        	scaleFac = getHeight() / (double)Game.HEIGHT;
			        	leftSide = (getWidth() - (Game.WIDTH * scaleFac)) / 2;
			        	frame.toBack(); // this seems to (sometimes) fix a bug in Java...
			        	frame.toFront();
			        }
			});
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			draw((Graphics2D) g);
		}
	}

	private Game game;
	private JFrame frame;
	private JPanel panel;
	
	// How to scale and translate the game to fit inside the window
	public static double leftSide = 0;
	public static double scaleFac = 1;

	public Application(String title, boolean launchGui, double desiredHeightOfWindow) {
		init(title, launchGui, desiredHeightOfWindow);
	}
	
	private void init(String title, boolean launchGui, double desiredHeightOfWindow){
		// Create Game
		game = new Game();
		
		// Preload resources
		Resources.preloadResources();
		
		if(launchGui){
			frame = new JFrame();
			
			// Screen dimensions
			Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
			double screenHeight = screenDimensions.height;
			double screenWidth = screenDimensions.width;

			// Create window
			frame.setTitle(title);
			frame.setLayout(new BorderLayout()); // auto resizes panel
			
			// Create draw panel
			panel = new DrawPanel();
			frame.add(panel, BorderLayout.CENTER);
		
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			
			// Fit on screen
			double desiredHeight = screenHeight * desiredHeightOfWindow;
			
			double scale = desiredHeight/(Game.HEIGHT);
			setGameSize(Game.WIDTH * scale, Game.HEIGHT * scale);
			
			frame.setLocation((int)(screenWidth/2 - frame.getWidth()/2),
					(int)(screenHeight*5.5/12.0 - frame.getHeight()/2));
			
			frame.setVisible(true);

			// Add input listeners
			InputListener inputListener = new Input.InputListener();
			frame.addKeyListener(inputListener);
			panel.addMouseListener(inputListener);
			panel.addMouseMotionListener(inputListener);
		} else {
			frame = null;
		}
	}
	
	private void setGameSize(double w, double h){
		
		frame.setVisible(true); // Need this or insets will be wrong
		Insets insets = frame.getInsets();
		
		int vertInsets = insets.top + insets.bottom;
		int horizInsets = insets.left + insets.right;
		
		int width = (int)(w + horizInsets);
		int height = (int)(h + vertInsets);
		
		frame.setSize(width, height);
		frame.setPreferredSize(new Dimension(width, height));
		frame.pack();
		Utils.log("Setting window size: " + width + " " + height);
	}

	// Called about 60 times a second
	// dt is the amount of time (in seconds) passed since last frame
	private void update(double dt) {
		game.update(dt);
	}

	private void draw(Graphics2D g) {

		AffineTransform prevTransform = g.getTransform();
		
		// Scale to fit window
		g.translate(leftSide, 0);
		g.scale(scaleFac, scaleFac);
		
		game.draw(g);
		
		g.setTransform(prevTransform);
		
		if(leftSide > 0){
			// Draw vertical bars on left and right
			g.setColor(Color.black);
			g.fillRect(0, 0, (int)leftSide+1, frame.getHeight());
			g.fillRect(frame.getWidth()-(int)leftSide-1, 0, (int)leftSide+1, frame.getHeight());			
		}
	}

	// Game loop -
	// Updates and redraws the window about 60 times a second
	// mostly copied from the internet
	private long desiredFPS = 60;
	private long desiredDeltaLoop = (1000 * 1000 * 1000) / desiredFPS;

	private boolean running = true;

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

			if(frame != null)
				frame.repaint(); // repaint the JFrame, calls paintComponent on our panel
			
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
