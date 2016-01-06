package network;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import engine.Observer;
import engine.Utils;

/**
 * Handles network on the client side. You can subscribe to messages of a given class,
 * and all those messages will be forwarded to you. Don't connect to the network until
 * all subscriptions have been set up!
 */
public class Client {

	private static DataTranslator translator = new DataTranslator();
	public static NetworkMessagePublisher publisher = new NetworkMessagePublisher(translator);
	
	private static SocketHandler socketHandler = null;
	
	public static String addr;
	public static int port;
	
	private static boolean connected = false;
	
	public static void connect(String addr, int port){
				
		Client.addr = addr;
		Client.port = port;
		
		connected = false;
		
		System.out.println("Client connecting to: " + addr + ":" + port);
		
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
		socketHandler = new SocketHandler(socket, publisher, translator);
		new Thread(socketHandler).start();
	}
	
	public static void update(){
		publisher.forwardQueuedMessages();
	}

	/**
	 * Subscribe the observer to messages of the given class. Cannot have multiple observers subscribed
	 * to one class, and must subscribe to all messages the Server might send before connecting. 
	 * Otherwise you won't be able to translate some of the incoming messages. 
	 */
	public static void subscribe(Class <? extends NetworkMessage> theClass, Observer observer){
		publisher.subscribe(theClass, observer);
	}
	
	/**
	 * Send the message to the server
	 */
	public static void sendMessage(NetworkMessage msg){
		if(connected)
			socketHandler.sendMessage(msg);
	}
}
