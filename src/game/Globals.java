package game;

import java.util.Date;

import engine.Game;
import server.Server;
import utils.Observer;
import network.Client;
import network.NetworkMessagePublisher;
import network.message.PingMessage;

/**
 * Place where we put globals. While it's bad practice to have
 * a lot of global variables, it's useful to have these things in one place
 */
public class Globals {
	
	/**
	 * Debug mode, as opposed to release mode
	 */
	public static boolean DEBUG = true;
	
	/**
	 * Debug mode - show fps, tile heats, collision boxes, misc info.
	 * DEV_MODE is enabled by hitting ` on the keyboard.
	 */
	public static boolean DEV_MODE = false;
	
	// Global variables
	
	public static Player player = null;
	public static Map map = null;
	public static PlayerManager playerManager = null;
	public static String desiredPlayerName = "";
	public static CollisionManager collisionManager = null;
	
	private static double gameStartTime = 0;
	private static double pingTime = 0;
	
	private static boolean isServer = false;
	
	
	public static void initGlobals(boolean server){		
		if(server){
			isServer = true;
		} 
		
		playerManager = new PlayerManager();
		collisionManager = new CollisionManager();
		
		// Load level from file
		Globals.map = new Map("TestLevel.txt");
				
		if(!isServer()) {
			Globals.player = new Player((short)-1);
		}
		
		
		// TODO shouldnt go here...
		
		publisher().subscribe(PingMessage.class, new Observer(){
			public void notify(Object arg){
				PingMessage msg = (PingMessage) arg;
				
				// Send requests back
				if(msg.request){
					msg.request = false;
					msg.gameTime = Globals.getNetworkGameTime();
					
					if(isServer){
						msg.client.sendMessage(msg);
					} else {
						Client.sendMessage(msg);
					}
				} else {
					long pingTimeLong = new Date().getTime() - msg.sentTime;
					pingTime = pingTimeLong / 1000.0;
					
					setGameTime(msg.gameTime + pingTime);
					
					System.out.println("Ping time = " + pingTime);
				}
			}
		});
	}
	
	/**
	 * Our best guess of what the game time is for the whole network
	 */
	public static double getNetworkGameTime(){
		double ret = Game.getTime() - gameStartTime;
//		if(!isServer()){
//			ret += pingTime/2;
//		}
		return ret;
	}
	
	public static void setGameTime(double t){
		gameStartTime = Game.getTime() - t;
	}
	
	public static String getPlayerName(){
		if(!isServer)
			return player.getName();
		else
			return "[Server]";
	}
	
	public static boolean isAuthoritative(){
		return isServer || !Client.isConnected();
	}
	
	/**
	 * Are we the server?
	 */
	public static boolean isServer(){
		return isServer;
	}
	
	/**
	 * Get the global Network Publisher from either Client or Server, 
	 * depending on which one we are
	 */
	public static NetworkMessagePublisher publisher(){
		if(isServer()){
			return Server.getPublisher();
		} else {
			return Client.publisher;
		}
	}

}
