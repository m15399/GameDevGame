package network;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import engine.Utils;
import game.GameDevGame;

/**
 * Handles network on the client side
 */
public class Client {

	private SocketHandler socketHandler;
	
	public String addr;
	public int port;
	
	private boolean connected;
	
	public Client(String addr, int port){
		this.addr = addr;
		this.port = port;
		
		connected = false;
		
		System.out.println("Starting client: " + addr + ":" + port);
		
		Socket socket;
		
		try {
			socket = new Socket(addr, port);
			connected = true;
			
		} catch (UnknownHostException e) {
			Utils.err("Unknown host");
			return;
		} catch (IOException e) {
			Utils.err("Failed to connect to server");
			return;
		}
		
		// Create a socket handler, forward messages to global clientPub
		socketHandler = new SocketHandler(socket, GameDevGame.clientPub);
		new Thread(socketHandler).start();
	}

	public void sendMessage(NetworkMessage msg){
		if(connected)
			socketHandler.sendMessage(msg);
	}
}
