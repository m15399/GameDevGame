package server;

import java.net.Socket;

import network.NetworkMessage;
import network.SocketHandler;
import engine.Observer;

/**
 * Stores information about the client and handles messages from it
 */
public class ClientHandler {
	
	private short playerNumber;
	private SocketHandler socketHandler;
	
	private Server server;
	
	public ClientHandler(Server theServer, Socket sock, short playerNumber){
		this.playerNumber = playerNumber;
		server = theServer;

		// Start a socket handler to receive incoming messages
		socketHandler = new SocketHandler(sock, theServer.getPublisher(), theServer.getTranslator());
		socketHandler.onDisconnect = new Observer(){
			public void notify(Object arg){
				wasDisconnected();
			}
		};
		new Thread(socketHandler).start();
	}
	
	public void wasDisconnected(){
		server.disconnectClient(this);
	}
	
	public boolean isConnected(){
		return socketHandler.isConnected();
	}
	
	public short getPlayerNumber(){
		return playerNumber;
	}
	
	/**
	 * Send a message to the client
	 */
	public void sendMessage(NetworkMessage msg){
		socketHandler.sendMessage(msg);
	}
}
