package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Test message
 */
public class TestNetworkMessage extends NetworkMessage implements Serializable{

	private static final long serialVersionUID = -6125001571418567863L;

	public String greeting;
	public float someNumber;
	
	public TestNetworkMessage(){}
	
	public TestNetworkMessage(String s, float f){
		greeting = s;
		someNumber = f;
	}
	
	public OpCode getOpcode() {
		return NetworkMessage.OpCode.TEST_MESSAGE;
	}
	
	public void readData(DataInputStream input) throws IOException {
		greeting = input.readUTF();
		someNumber = input.readFloat();
	}

	public void writeData(DataOutputStream output) throws IOException {
		output.writeUTF(greeting);
		output.writeFloat(someNumber);
	}

	public String toString(){
		return greeting + " ; " + someNumber;
	}

}
