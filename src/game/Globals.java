package game;

import server.Server;
import network.Client;
import network.NetworkMessagePublisher;

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
	
	private static boolean isServer = false;
	
	
	public static void initGlobals(){		
		playerManager = new PlayerManager();
		
		// Load level from file
		Globals.map = new Map("TestLevel.txt");
		
		Globals.player = new Player((short)-1);
	}
	
	/**
	 * Create only the globals we need to run the server (no player, etc)
	 */
	public static void initGlobalsForServer(){
		isServer = true;
		
		playerManager = new PlayerManager();

		Globals.map = new Map("TestLevel.txt");
	}
	
	/**
	 * Are we a client who is online?
	 */
	public static boolean isOnlineClient(){
		return Client.isConnected();
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
