package game;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.sound.sampled.Clip;

import utils.Observer;
import utils.Utils;

import main_menu.MainMenu;
import network.Client;
import network.ServerGreetingMessage;
import network.TileUpdatesMessage;

import engine.*;

public class GameDevGame extends GameObject {

	public static final String VERSION = "0.1";
	
	// Default startup settings
	private static double WINDOW_HEIGHT = -1;
	private static String AUTO_JOIN_ADDR = "";
	private static int AUTO_JOIN_PORT = -1;
	private static boolean MUSIC = true;
	
	// Starts the game!
	public static void main(String[] args) {
		
		// Set debug options here
//		WINDOW_HEIGHT = .4;
//		MUSIC = false;
//		AUTO_JOIN_ADDR = "localhost";
//		AUTO_JOIN_PORT = 8000;
		
		if(WINDOW_HEIGHT > 0)
			Application.launch("GameDevGame", true, WINDOW_HEIGHT);
		else
			Application.launch("GameDevGame", true);
		
		if(AUTO_JOIN_ADDR.length() > 0){
			new GameDevGame(AUTO_JOIN_ADDR, AUTO_JOIN_PORT);		
		} else {
			new MainMenu();
		}
	}
	
	private String connectAddress;
	private int port;
	
	public GameDevGame(){
		init("", -1);
	}
	
	public GameDevGame(String address, int thePort) {
		init(address, thePort);
	}
	
	private void init(String address, int thePort){
		connectAddress = address;
		port = thePort;
		
		// Network stuff
		
		Client.publisher.subscribe(ServerGreetingMessage.class, new Observer(){
			public void notify(Object arg){
				ServerGreetingMessage msg = (ServerGreetingMessage)arg;
				Utils.log("I am player number " + msg.playerNumber);
				
				// Recreate the player with the correct playerNumber
				Globals.player.destroy();
				Globals.player = new Player(msg.playerNumber);
				Globals.player.setName(Globals.desiredPlayerName);
			}
		});
		
		// Update our map when server changes tiles
		Client.publisher.subscribe(TileUpdatesMessage.class, new Observer(){
			public void notify(Object arg){
				TileUpdatesMessage msg = (TileUpdatesMessage) arg;
				Globals.map.updateTileStates(msg);
			}
		});
	}
	
	public void onStart(){
		// Play some music
		if(MUSIC){
			Clip c = Resources.getSound("test.wav");
			Utils.setClipVolume(c, -5f);
			c.loop(Clip.LOOP_CONTINUOUSLY);			
		}
		
		// Create globals like map, player
		Globals.initGlobals();
		
		new Camera();
		new Background("background.png");
		
		new HeatMeter();
		new WeaponMeter();
		
		// test pickups
		new WeaponPickup(BowAndArrow.class, 64 * 2.5, 64 * 2.5);
		new WeaponPickup(BowAndArrow.class, 64 * 0.5, 64 * 4.5);
		
		setDrawOrder(1000);
		
		new Chat();
		
		// Connect if ip address was set
		if(connectAddress.length() > 0){
			Client.setAddress(connectAddress, port);
			Client.connect();
		}
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
