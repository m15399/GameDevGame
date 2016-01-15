package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Server sends this greeting to clients when they join. Contains their 
 * playerNumber
 */
public class ServerGreetingMessage extends NetworkMessage {

	private static final long serialVersionUID = 3563739124719660308L;

	public short playerNumber;
	
	public ServerGreetingMessage(){}
	
	public ServerGreetingMessage(short pNum){
		playerNumber = pNum;
	}
	
	@Override
	public OpCode getOpcode() {
		return NetworkMessage.OpCode.SERVER_GREETING;
	}

	@Override
	public void readData(DataInputStream input) throws IOException {
		playerNumber = input.readShort();
	}

	@Override
	public void writeData(DataOutputStream output) throws IOException {
		output.writeShort(playerNumber);
	}

}