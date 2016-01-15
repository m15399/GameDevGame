package game;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.sound.sampled.Clip;

import network.Client;
import network.ServerGreetingMessage;
import network.TileHeatUpdatesMessage;

import engine.*;

public class GameDevGame extends GameObject {

	public static final String VERSION = "0.1";
	
	// Starts the game!
	public static void main(String[] args) {		
		Application.launch("GameDevGame", true, .8);
		new GameDevGame();
	}

	// Global variables - it's likely these will be moved later
		
	public void onStart() {
		// Play some music
		Clip c = Resources.getSound("test.wav");
		Utils.setClipVolume(c, -5f);
		c.loop(Clip.LOOP_CONTINUOUSLY);
		
		// Create globals like map, player
		Globals.initGlobals();
		
		new Camera();
		new Background("background.png");
		
		new HeatMeter();
		
		setDrawOrder(1000);

		
		// Network stuff
		
		Client.publisher.subscribe(ServerGreetingMessage.class, new Observer(){
			public void notify(Object arg){
				ServerGreetingMessage msg = (ServerGreetingMessage)arg;
				Utils.log("I am player number " + msg.playerNumber);
				
				// Recreate the player with the correct playerNumber
				Globals.player.destroy();
				Globals.player = new Player(msg.playerNumber);
			}
		});
		
		// Update our map when server changes tiles
		Client.publisher.subscribe(TileHeatUpdatesMessage.class, new Observer(){
			public void notify(Object arg){
				TileHeatUpdatesMessage msg = (TileHeatUpdatesMessage) arg;
				Globals.map.updateTileHeats(msg);
			}
		});
		
		Client.setAddress("localhost", 8000);
		Client.connect();
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
