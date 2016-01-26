package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChatMessage extends NetworkMessage {

	private static final long serialVersionUID = 3837168201469824244L;

	public String name;
	public String message;
	
	public ChatMessage(){}
	
	public ChatMessage(String name, String msg){
		this.name = name;
		message = msg;
	}
	
	@Override
	public OpCode getOpcode() {
		return NetworkMessage.OpCode.CHAT;
	}

	@Override
	public void readData(DataInputStream input) throws IOException {
		name = input.readUTF();
		message = input.readUTF();
	}

	@Override
	public void writeData(DataOutputStream output) throws IOException {
		output.writeUTF(name);
		output.writeUTF(message);
	}
	
	
	
}
