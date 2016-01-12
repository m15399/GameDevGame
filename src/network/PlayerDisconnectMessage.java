package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerDisconnectMessage extends NetworkMessage {

	private static final long serialVersionUID = 4808794145615085747L;

	public short playerNumber;
	
	public PlayerDisconnectMessage(){}
	
	public PlayerDisconnectMessage(short pNum){
		playerNumber = pNum;
	}
	
	@Override
	public OpCode getOpcode() {
		return NetworkMessage.OpCode.PLAYER_DC;
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
