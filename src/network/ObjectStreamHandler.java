package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import engine.Utils;

/**
 * Sends objects over a socket. Currently not being used, but we might have to fall back on it later,
 * so don't remove it
 */
public class ObjectStreamHandler extends StreamHandler {
	
	private ObjectOutputStream output;
	private ObjectInputStream input;
	
	public ObjectStreamHandler(SocketHandler socketHandler, DataTranslator translator){
		super(socketHandler, translator);
		
		Utils.err("ObjectStreamHandler should not be used (it's just a fallback) - use DataStreamHandler instead");
		
		try {
			output = new ObjectOutputStream(socketHandler.getOutputStream());
			input = new ObjectInputStream(socketHandler.getInputStream());
		} catch (IOException e1) {
			Utils.err("Failded to create input/output sockets");
			return;
		}
	}
	
	public void sendMessage(NetworkMessage msg){
		try {
			output.writeObject(msg);
		} catch (IOException e2) {
			Utils.err("Unable to send message");
		}
	}
	
	public NetworkMessage readObject(){
		NetworkMessage msg;
		try {
			msg = (NetworkMessage) input.readObject();
			return msg;
		} catch (ClassNotFoundException e) {
			Utils.err("Expected object of type NetworkMessage");
		} catch (IOException e) {
			Utils.err("Failed to read object");
			Utils.err("Connection to client *probably* lost");
		}
		return null;
	}
}
