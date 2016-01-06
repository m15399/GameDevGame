package game;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.sound.sampled.Clip;

import network.Client;
import network.TestNetworkMessage;

import engine.*;

public class GameDevGame extends GameObject {

	public static final String VERSION = "0.1";
	
	// Starts the game!
	public static void main(String[] args) {
		Application.launch("GameDevGame");
		new GameDevGame();
	}

	// Global variables - it's likely these will be moved later
	
	public static Player player;
	public static Map map;
	
	private Camera camera;
		
	public void onStart() {
		// Play some music
		Clip c = Resources.getSound("test.wav");
		Utils.setClipVolume(c, -5f);
//		c.loop(Clip.LOOP_CONTINUOUSLY);
		
		camera = new Camera();
		new Background("background.png");
		
		// Load level from file
		map = new Map("TestLevel.txt");
		
		player = new Player();
		
		// Tell camera to follow the player
		camera.setTarget(player);
		
		new HeatMeter();
		
		setDrawOrder(1000);

		
		// Network stuff
		Client.subscribe(TestNetworkMessage.class, new Observer(){
			public void notify(Object arg){
				TestNetworkMessage msg = (TestNetworkMessage) arg;
				System.out.println("GameDevGame recieved test message: " + msg);
			}
		});
		
		Client.connect("localhost", 8000);
		
		// Send test message
		Client.sendMessage(new TestNetworkMessage("Hello from GameDevGame!", 42.42f));
	}
	
	public void update(double dt){
		Client.update();
	}
	
	public void draw(Graphics2D g){
		// Display the version number for the first few seconds of the game
		if(Game.time < 5){
			g.setColor(Color.white);
			Utils.drawStringCentered(g, "v" + VERSION, Game.WIDTH - 22, Game.HEIGHT - 12);
		}
	}

}
