package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import network.DataTranslator;
import network.NetworkMessage;
import network.NetworkMessagePublisher;
import network.PlayerUpdateMessage;
import network.ServerGreetingMessage;

import engine.Observer;
import engine.Utils;

/**
 * Server main class - runs the Server.
 */
public class Server implements Runnable {

	public static void main(String[] args){
		
		boolean gui = true;
		
		// Parse command line args
		for(String arg : args){
			if(arg.equals("-nogui")){
				gui = false;
			}
		}
				
		new Logger(gui);
		
		Server server = new Server(8000);
		new Thread(server).start();
	}
		
	public int port;
	private ServerSocket serverSocket;
	
	private NetworkMessagePublisher serverPub;
	private DataTranslator translator;
	
	private ArrayList<ClientHandler> handlers;
	
	private int currPlayerNumber = 0;

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
		
		
		Utils.log("Starting server on port: " + port);
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			Utils.fatal("Failed to create server socket");
		}
	}
	
	/**
	 * Forward the message to all players, except the one with playerNumber == except
	 */
	private void forwardToAll(NetworkMessage msg, int except){
		ClientHandler remove = null;
		
		for(ClientHandler h : handlers){
			if(!h.isConnected()){
				remove = h;
			} else if(h.getPlayerNumber() != except){
				h.sendMessage(msg);
			}
		}
		
		if(remove != null){
			handlers.remove(remove);
			Utils.log("Detected disconnect: player #" + remove.getPlayerNumber());
		}
	}
	
	/**
	 * Forward the message to all players
	 */
	public void forwardToAll(NetworkMessage msg){
		forwardToAll(msg, -1); // playerNumber should never be -1
	}
	
	public void run() {
		
		// Listen for new connections
		while (true) {
			Socket sock;
			try {
				sock = serverSocket.accept();
				Utils.log("A user connected");

				currPlayerNumber++;

				// Start a handler for each user
				ClientHandler handler = new ClientHandler(currPlayerNumber, sock, serverPub, translator);
				handlers.add(handler);
				
				// Greeting message				
				handler.sendMessage(new ServerGreetingMessage(currPlayerNumber));
				
			} catch (IOException e) {
				Utils.err("Failed to accept socket");
			}
		}
	}
}
