package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Server sends this greeting to clients when they join. Contains their 
 * playerNumber
 */
public class ServerGreetingMessage extends NetworkMessage {
	
	public short playerNumber;
	
	public ServerGreetingMessage(){}
	
	public ServerGreetingMessage(short pNum){
		playerNumber = pNum;
	}

	public void readData(DataInputStream input) throws IOException {
		playerNumber = input.readShort();
	}

	public void writeData(DataOutputStream output) throws IOException {
		output.writeShort(playerNumber);
	}

}
