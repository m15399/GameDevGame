package game;

import network.Client;

/**
 * Place where we put globals. While it's bad practice to have
 * a lot of global variables, it's useful to have these things in one place
 */
public class Globals {
	public static Player player;
	public static Map map;
	public static PlayerManager playerManager;
	
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
		Globals.map = new Map("TestLevel.txt");
	}
	
	public static boolean isOnlineGame(){
		return Client.isConnected();
	}
}
