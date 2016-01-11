package server;

import java.net.Socket;

import network.DataTranslator;
import network.NetworkMessage;
import network.NetworkMessagePublisher;
import network.SocketHandler;

/**
 * Stores information about the client and handles messages from it
 */
public class ClientHandler {
	
	private int playerNumber;
	private SocketHandler socketHandler;
	
	public ClientHandler(int playerNumber, Socket sock, NetworkMessagePublisher pub, DataTranslator translator){
		this.playerNumber = playerNumber;

		// Start a socket handler to receive incoming messages
		socketHandler = new SocketHandler(sock, pub, translator);
		new Thread(socketHandler).start();
	}
	
	public boolean isConnected(){
		return socketHandler.isConnected();
	}
	
	public int getPlayerNumber(){
		return playerNumber;
	}
	
	/**
	 * Send a message to the client
	 */
	public void sendMessage(NetworkMessage msg){
		socketHandler.sendMessage(msg);
	}
}
