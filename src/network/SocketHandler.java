package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import engine.Utils;

/**
 * Helps deal with Socket objects - sends outgoing messages, forwards 
 * incoming messages to a NetworkMessagePublisher
 */
public class SocketHandler implements Runnable {

	private Socket socket;

	private ObjectOutputStream output;
	private ObjectInputStream input;
	
	private NetworkMessagePublisher publisher;

	public SocketHandler(Socket sock, NetworkMessagePublisher publisher) {
		socket = sock;
		this.publisher = publisher;

		try {
			output = new ObjectOutputStream(socket.getOutputStream());
			input = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e1) {
			Utils.err("Failded to create input/output sockets");
			return;
		}
	}

	/**
	 * Send a message through the output socket
	 */
	public synchronized void sendMessage(NetworkMessage msg){
		try {
			output.writeObject(msg);
		} catch (IOException e2) {
			Utils.err("Unable to send message");
		}
	}
	
	public void run() {
		
		// Listen for incoming messages, forward them to the publisher
		while (true) {
			try {
				NetworkMessage msg = (NetworkMessage) input.readObject();
				publisher.takeMessage(msg);

			} catch (ClassNotFoundException e) {
				Utils.err("Expected object of type NetworkMessage");
			} catch (IOException e) {
				Utils.err("Failed to read object");
				Utils.err("Connection to client *probably* lost - disconnecting");
				try {
					socket.close();
				} catch (IOException e1) {
					Utils.err("Failed to close socket");
				}
				return;
			}
		}

	}
}
