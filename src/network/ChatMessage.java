package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChatMessage extends NetworkMessage {
	
	public String name;
	public String message;
	
	public ChatMessage(){}
	
	public ChatMessage(String name, String msg){
		this.name = name;
		message = msg;
	}

	public void readData(DataInputStream input) throws IOException {
		name = input.readUTF();
		message = input.readUTF();
	}

	public void writeData(DataOutputStream output) throws IOException {
		output.writeUTF(name);
		output.writeUTF(message);
	}
}
