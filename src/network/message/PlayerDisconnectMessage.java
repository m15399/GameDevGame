package network.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerDisconnectMessage extends NetworkMessage {
	
	public short playerNumber;
	
	public PlayerDisconnectMessage(){}
	
	public PlayerDisconnectMessage(short pNum){
		playerNumber = pNum;
	}
	
	public void readData(DataInputStream input) throws IOException {
		playerNumber = input.readShort();
	}

	public void writeData(DataOutputStream output) throws IOException {
		output.writeShort(playerNumber);
	}

}
