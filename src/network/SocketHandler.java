package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import network.message.NetworkMessage;

import server.ClientHandler;
import utils.Observer;
import utils.Utils;


/**
 * Helps deal with Socket objects - sends outgoing messages, forwards 
 * incoming messages to a NetworkMessagePublisher
 */
public class SocketHandler implements Runnable {

	private Socket socket;
	
	private StreamHandler streamHandler;
	
	private NetworkMessagePublisher publisher;
	
	private boolean connected;
	
	public Observer onDisconnect;
	
	// Used to tag messages with who we got them from
	public ClientHandler client = null;

	public SocketHandler(Socket sock, NetworkMessagePublisher publisher) {
		socket = sock;
		this.publisher = publisher;

		streamHandler = new DataStreamHandler(this);
		
		connected = true;
		
		onDisconnect = null;
	}
	
	public boolean isConnected(){
		return connected;
	}
	
	public OutputStream getOutputStream(){
		try {
			return socket.getOutputStream();
		} catch (IOException e) {
			Utils.err("Couldn't get output stream");
		}
		return null;
	}
	
	public InputStream getInputStream(){
		try {
			return socket.getInputStream();
		} catch (IOException e) {
			Utils.err("Couldn't get input stream");
		}
		return null;
	}

	/**
	 * Send a message through the output socket
	 */
	public synchronized void sendMessage(NetworkMessage msg){
		int err = streamHandler.sendMessage(msg);
		if(err != 0){
			Utils.err("Failed to send message to socket, disconnecting");
			disconnect();
		}
	}
	
	private synchronized void disconnect(){
		if(!connected)
			return;
		
		try {
			socket.close();
		} catch (IOException e) {
			Utils.err("Failed to close socket");
		}
		connected = false;
		
		
		if(onDisconnect != null){
			onDisconnect.notify(null);
		}
	}
	
	public void run() {
		// Listen for incoming messages, forward them to the publisher
		while (true) {
			NetworkMessage msg = streamHandler.readObject();
			
			if(msg != null){
				msg.client = client;
				publisher.takeMessage(msg);
			} else {
				Utils.err("Failed to read message from socket, disconnecting");
				disconnect();
				return;
			}
		}
	}
}
