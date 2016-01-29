package server;

import java.net.Socket;

import utils.Observer;

import network.NetworkMessage;
import network.SocketHandler;

/**
 * Stores information about the client and handles messages from it
 */
public class ClientHandler {
	
	private short playerNumber;
	private SocketHandler socketHandler;
		
	public ClientHandler(Socket sock, short playerNumber){
		this.playerNumber = playerNumber;

		// Start a socket handler to receive incoming messages
		socketHandler = new SocketHandler(sock, Server.getPublisher());
		socketHandler.onDisconnect = new Observer(){
			public void notify(Object arg){
				wasDisconnected();
			}
		};
		new Thread(socketHandler).start();
	}
	
	public void wasDisconnected(){
		Server.disconnectClient(this);
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
