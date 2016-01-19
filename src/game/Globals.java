package game;

import server.Server;
import network.Client;
import network.NetworkMessagePublisher;

/**
 * Place where we put globals. While it's bad practice to have
 * a lot of global variables, it's useful to have these things in one place
 */
public class Globals {
	public static Player player;
	public static Map map;
	public static PlayerManager playerManager;
	
	private static boolean isServer;
	
	private static void initAll(){
		playerManager = null;
		player = null;
		map = null;
		isServer = false;
	}
	
	public static void initGlobals(){
		initAll();
		
		playerManager = new PlayerManager();
		
		// Load level from file
		Globals.map = new Map("TestLevel.txt");
		
		Globals.player = new Player((short)-1);
	}
	
	/**
	 * Create only the globals we need to run the server (no player, etc)
	 */
	public static void initGlobalsForServer(boolean simulatePlayers){
		initAll();
		isServer = true;
		
		if(simulatePlayers)
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
