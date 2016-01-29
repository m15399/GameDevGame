package server;

import java.awt.Graphics2D;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import utils.Observer;
import utils.Utils;

import network.ChatMessage;
import network.DataTranslator;
import network.MapStateMessage;
import network.NetworkMessage;
import network.NetworkMessagePublisher;
import network.PlayerDisconnectMessage;
import network.PlayerUpdateMessage;
import network.ServerGreetingMessage;
import network.TileUpdatesMessage;

import engine.Application;
import engine.GameObject;
import game.Globals;
import game.Tile;

/**
 * Server main class - runs the Server.
 */
public class Server {

	public static void main(String[] args){
		
		boolean launchGui = true;
		boolean createLogFile = false;
		
		// Parse command line args
		for(String arg : args){
			if(arg.equals("-nogui")){
				launchGui = false;
			}
		}
		
		// Launch a server log window 
		new Logger(launchGui, createLogFile);

		// Launch the game engine
		Application.launch("Server Monitor", launchGui, .4);
		Application.gameFrame.setLocation(100, 350);
		
		// Start server
		Server.initServer(8000);
	}
	
	
	
	private static ServerSocket serverSocket;
	
	private static NetworkMessagePublisher serverPub;
	private static DataTranslator translator;
	
	private static ArrayList<ClientHandler> handlers;
	
	private static short currPlayerNumber = 0;

	public static void initServer(int port) {		
		translator = new DataTranslator();
		serverPub = new NetworkMessagePublisher();
		serverPub.forwardImmediately = true;
		
		handlers = new ArrayList<ClientHandler>();
		
		// Forward PlayerUpdateMessages to everyone except that player
		serverPub.subscribe(PlayerUpdateMessage.class, new Observer(){
			public void notify(Object arg){
				PlayerUpdateMessage msg = (PlayerUpdateMessage) arg;				
				
				if(Globals.playerManager != null)
					Globals.playerManager.processUpdateMessage(msg);
				
				if(msg.playerNumber >= 0){
					forwardToAll(msg, msg.playerNumber);
				}
			}
		});
		
		// Update our map when players heat up tiles
		serverPub.subscribe(TileUpdatesMessage.class, new Observer(){
			public void notify(Object arg){
				TileUpdatesMessage msg = (TileUpdatesMessage) arg;
				Globals.map.updateTileStates(msg);
			}
		});
		
		serverPub.subscribe(ChatMessage.class, new Observer(){
			public void notify(Object arg){
				ChatMessage msg = (ChatMessage) arg;
				Utils.log(msg.name + " says: " + msg.message);
				forwardToAll(msg);
			}
		});
		
		
		Utils.log("Starting server on port: " + port);
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			Utils.fatal("Failed to create server socket");
		}
		
		// Create the server's game simulation
		new ServerGameObject();
		
		// Start a thread listening for connections
		new Thread(new Runnable(){
			public void run() {
				// Listen for new connections
				while (true) {
					Socket sock;
					try {
						sock = serverSocket.accept();
						playerConnected(sock);
						
					} catch (IOException e) {
						Utils.err("Failed to accept socket");
					}
				}
			}
		}).start();
	}
	
	public static synchronized void playerConnected(Socket sock){
		currPlayerNumber++;

		Utils.log("A user connected: Player #" + currPlayerNumber);

		// Start a handler for each user
		ClientHandler handler = new ClientHandler(sock, currPlayerNumber);
		handlers.add(handler);
		
		// Greeting message				
		handler.sendMessage(new ServerGreetingMessage(currPlayerNumber));
		
		// Send the entire map state to new player
		MapStateMessage mapState = new MapStateMessage();
		Globals.map.writeEntireMapState(mapState);
		handler.sendMessage(mapState);
	}
	
	public static DataTranslator getTranslator(){
		return translator;
	}
	
	public static NetworkMessagePublisher getPublisher(){
		return serverPub;
	}
	
	public static synchronized void disconnectClient(ClientHandler client){
		handlers.remove(client);
		Utils.log("Disconnecting player #" + client.getPlayerNumber());
		
		if(Globals.playerManager != null){
			Globals.playerManager.destroyAndRemovePlayer(client.getPlayerNumber());
		}
		
		forwardToAll(new PlayerDisconnectMessage(client.getPlayerNumber()));
	}
	
	/**
	 * Forward the message to all players, except the one with playerNumber == except
	 */
	private static synchronized void forwardToAll(NetworkMessage msg, short except){
		ClientHandler remove = null;
		
		for(ClientHandler h : handlers){
			if(!h.isConnected()){
				remove = h;
			} else if(h.getPlayerNumber() != except){
				h.sendMessage(msg);
			}
		}
		
		if(remove != null){
			disconnectClient(remove);
		}
	}
	
	/**
	 * Forward the message to all players
	 */
	public static synchronized void forwardToAll(NetworkMessage msg){
		forwardToAll(msg, (short)-1); // playerNumber should never be -1
	}
	
	
	
	private static class ServerGameObject extends GameObject {
		
		public void onStart(){
			setDrawOrder(-1000);
			
			// Start the game simulation
			Globals.initGlobalsForServer();
		}
		
		public void draw(Graphics2D g){
			// Draw the game zoomed out so we can see the whole map
			double scaleFac = .5;
			g.scale(scaleFac, scaleFac);
			g.translate(Tile.SIZE, Tile.SIZE);
		}
	}
	
}
