package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import engine.Utils;

/**
 * Translates NetworkMessages to a DataStream and vice-versa. You must register a valid NetworkMessage 
 * class before it can be translated. If the translator encounters an opcode for a message that hasn't
 * been registered, it will quit the program with a fatal error
 */
public class DataTranslator {

	/**
	 * Maps byte codes to NetworkMessage classes
	 */
	private HashMap<Byte, Class<? extends NetworkMessage> > map;
	
	public DataTranslator(){
		map = new HashMap<Byte, Class<? extends NetworkMessage>>();
	}
	
	public void registerClass(Class<? extends NetworkMessage> theClass){
		byte opcode = -1;
		try {
			opcode = theClass.newInstance().getOpcodeByte();
		} catch (InstantiationException e) {
			Utils.fatal("Could not instantiate class. Might not have a constructor with no args");
		} catch (IllegalAccessException e) {
			Utils.fatal("Could not instantiate class. Might not have a constructor with no args");			
		}
		
		map.put(opcode, theClass);
	}
	
	public Class<? extends NetworkMessage> getClassForOpcode(byte opcode){
		return map.get(opcode);
	}
	
	public NetworkMessage readMessage(DataInputStream input){
		try {
			// The first byte is the opcode
			byte opcode = input.readByte();
			
			// Then, using the correct network message class, 
			// grab the rest of the data from the stream
			Class<? extends NetworkMessage> theClass = getClassForOpcode(opcode);
			if(theClass != null){
				NetworkMessage msg = theClass.newInstance();
				msg.readData(input); // read the data
				return msg;
			} else {
				// At this point we don't know how to read the stream and it is unusable
				Utils.fatal("Unable to translate opcode: " + opcode);
			}
		} catch (IOException e) {
			// stream was likely closed, ignore and return null
		} catch (InstantiationException e) {
			Utils.err("Unable to instantiate class, possibly no empty constructor");
		} catch (IllegalAccessException e) {
			Utils.err("Unable to instantiate class, possibly no empty constructor");			
		}
		return null;
	}
	
	public void writeMessage(NetworkMessage msg, DataOutputStream output){
		try {
			// First write the opcode
			output.writeByte(msg.getOpcodeByte());
			
			// Then use the NetworkMessage to write the rest of the data to output
			msg.writeData(output);
		} catch (IOException e) {
			Utils.err("Translator unable to write to socket");
		}
	}
	
}
