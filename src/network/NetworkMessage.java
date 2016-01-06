package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Abstract base class for network messages. You can extend this to make a new type of NetworkMessage
 */
public abstract class NetworkMessage implements Serializable {

	private static final long serialVersionUID = 3148088761987838447L;

	/**
	 * Represents the opcode for a message. Whenever you create a new class of message, 
	 * you must create a new OpCode so we don't have collisions
	 */
	public static enum OpCode {
		TEST_MESSAGE;
		
		public byte getValue(){
			return (byte)ordinal();
		}
	}
	
	/**
	 * Byte value of the opcode
	 */
	public byte getOpcodeByte(){
		return getOpcode().getValue();
	}
	
	/**
	 * Returns the opcode for this message
	 */
	public abstract OpCode getOpcode();
	
	/**
	 * Reads the data we expect from the DataInputStream
	 */
	public abstract void readData(DataInputStream input) throws IOException;
	
	/**
	 * Writes the message's data to the stream
	 */
	public abstract void writeData(DataOutputStream output) throws IOException;
	
}
