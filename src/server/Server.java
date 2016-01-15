package server;

import java.awt.Graphics2D;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import network.DataTranslator;
import network.NetworkMessage;
import network.NetworkMessagePublisher;
import network.PlayerDisconnectMessage;
import network.PlayerUpdateMessage;
import network.ServerGreetingMessage;
import network.TileHeatUpdatesMessage;

import engine.Application;
import engine.GameObject;
import engine.Observer;
import engine.Utils;
import game.Globals;
import game.Tile;

/**
 * Server main class - runs the Server.
 */
public class Server extends GameObject implements Runnable {

	public static void main(String[] args){
		
		boolean launchGui = true;
		
		// Parse command line args
		for(String arg : args){
			if(arg.equals("-nogui")){
				launchGui = false;
			}
		}
		
		// Launch a server log window 
		new Logger(launchGui);

		// Launch the game engine
		Application.launch("Server Monitor", launchGui, .4);
		
		
		Server server = new Server(8000);
		new Thread(server).start();
	}
	
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
	
	public int port;
	private ServerSocket serverSocket;
	
	private NetworkMessagePublisher serverPub;
	private DataTranslator translator;
	
	private ArrayList<ClientHandler> handlers;
	
	private short currPlayerNumber = 0;

	public Server(int port) {
		this.port = port;
		
		translator = new DataTranslator();
		serverPub = new NetworkMessagePublisher(translator);
		serverPub.forwardImmediately = true;
		
		handlers = new ArrayList<ClientHandler>();
		
		
		// Forward PlayerUpdateMessages to everyone except that player
		serverPub.subscribe(PlayerUpdateMessage.class, new Observer(){
			public void notify(Object arg){
				PlayerUpdateMessage msg = (PlayerUpdateMessage) arg;
				if(msg.playerNumber >= 0){
					forwardToAll(msg, msg.playerNumber);
				}
			}
		});
		
		// Update our map when players heat up tiles
		serverPub.subscribe(TileHeatUpdatesMessage.class, new Observer(){
			public void notify(Object arg){
				TileHeatUpdatesMessage msg = (TileHeatUpdatesMessage) arg;
				Globals.map.updateTileHeats(msg);
			}
		});
		
		
		Utils.log("Starting server on port: " + port);
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			Utils.fatal("Failed to create server socket");
		}
	}
	
	public DataTranslator getTranslator(){
		return translator;
	}
	
	public NetworkMessagePublisher getPublisher(){
		return serverPub;
	}
	
	public synchronized void disconnectClient(ClientHandler client){
		handlers.remove(client);
		Utils.log("Disconnecting player #" + client.getPlayerNumber());
		
		forwardToAll(new PlayerDisconnectMessage(client.getPlayerNumber()));
	}
	
	/**
	 * Forward the message to all players, except the one with playerNumber == except
	 */
	private void forwardToAll(NetworkMessage msg, short except){
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
	public void forwardToAll(NetworkMessage msg){
		forwardToAll(msg, (short)-1); // playerNumber should never be -1
	}
	
	public void run() {
		
		// Listen for new connections
		while (true) {
			Socket sock;
			try {
				sock = serverSocket.accept();

				currPlayerNumber++;

				Utils.log("A user connected: player #" + currPlayerNumber);


				// Start a handler for each user
				ClientHandler handler = new ClientHandler(this, sock, currPlayerNumber);
				handlers.add(handler);
				
				// Greeting message				
				handler.sendMessage(new ServerGreetingMessage(currPlayerNumber));
				
			} catch (IOException e) {
				Utils.err("Failed to accept socket");
			}
		}
	}
}
