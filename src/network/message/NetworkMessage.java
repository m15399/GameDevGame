package network.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import server.ClientHandler;

/**
 * Abstract base class for network messages. You can extend this to make a new type of NetworkMessage
 */
public abstract class NetworkMessage {
	
	/**
	 * Who we got the message from
	 */
	public ClientHandler client = null;
	
	/**
	 * Reads the data we expect from the DataInputStream
	 */
	public abstract void readData(DataInputStream input) throws IOException;
	
	/**
	 * Writes the message's data to the stream
	 */
	public abstract void writeData(DataOutputStream output) throws IOException;
	
}
