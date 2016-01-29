package network;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import utils.Utils;


/**
 * Handles network on the client side. You can subscribe to messages of a given class,
 * and all those messages will be forwarded to you. Don't connect to the network until
 * all subscriptions have been set up!
 */
public class Client {

	public static NetworkMessagePublisher publisher = new NetworkMessagePublisher();
	
	private static SocketHandler socketHandler = null;
	
	public static String addr;
	public static int port;
	
	private static boolean connected = false;
	
	public static void setAddress(String addr, int port){
		Client.addr = addr;
		Client.port = port;		
	}
	
	public static void connect(){
		
		connected = false;
		
		Utils.log("Client connecting to: " + addr + ":" + port);
		
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
		
		// Create a socket handler, forward messages to global clientPub, translate with the translator
		socketHandler = new SocketHandler(socket, publisher);
		new Thread(socketHandler).start();
	}
	
	private static void checkConnection(){
		if(connected && socketHandler != null && !socketHandler.isConnected())
			connected = false;
	}
	
	public static boolean isConnected(){
		Client.checkConnection();
		return connected;
	}
	
	public static void update(){
		publisher.forwardQueuedMessages();
	}
	
	/**
	 * Send the message to the server
	 */
	public static void sendMessage(NetworkMessage msg){
		if(isConnected())
			socketHandler.sendMessage(msg);
	}
}
