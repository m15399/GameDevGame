package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import utils.ClassToIntMapper;
import utils.Utils;


/**
 * Translates NetworkMessages to a DataStream and vice-versa. You must register a valid NetworkMessage 
 * class before it can be translated. If the translator encounters an opcode for a message that hasn't
 * been registered, it will quit the program with a fatal error.
 * Right now we're registering classes in a static block near the top of this class, just add
 * any new network message classes there. 
 */
public class DataTranslator {

	private static ClassToIntMapper networkMessageToIntMapper = new ClassToIntMapper();
	private static HashMap<Byte, Class<? extends NetworkMessage> > byteToNetworkMessageMap = null;

	/*
	 * ALL NETWORK MESSAGE CLASSES MUST BE REGISTERED HERE
	 */
	static {
		// Register classes
		registerClass(ChatMessage.class);
		registerClass(MapStateMessage.class);
		registerClass(PlayerDisconnectMessage.class);
		registerClass(PlayerUpdateMessage.class);
		registerClass(ServerGreetingMessage.class);
		registerClass(TileUpdatesMessage.class);
		
	}
	
	private static void registerClass(Class<? extends NetworkMessage> theClass){
		networkMessageToIntMapper.registerClass(theClass);
	}
	
	/**
	 * Takes the networkMessageToIntMapper and creates a map going in the other direction.
	 * Should be called after the first mapper has been totally filled up
	 */
	private static void initializeByteToNetworkMessageMap(){
		byteToNetworkMessageMap = new HashMap<Byte, Class<? extends NetworkMessage>>();
		
		ArrayList<Class<?>> classes = networkMessageToIntMapper.getClassList();
		
		for(Class<?> theClass : classes){
			int code = networkMessageToIntMapper.getClassNumber(theClass);
			
			@SuppressWarnings("unchecked")
			Class<? extends NetworkMessage> networkMessageClass = (Class<? extends NetworkMessage>)theClass;
			
			byteToNetworkMessageMap.put((byte)code, networkMessageClass);
		}
	}

	/**
	 * Opcode -> Class
	 */
	private static Class<? extends NetworkMessage> getClassForOpcode(byte opcode){
		if(byteToNetworkMessageMap == null)
			initializeByteToNetworkMessageMap();
		
		return byteToNetworkMessageMap.get(opcode);
	}
	
	/**
	 * Class -> Opcode
	 */
	private static byte getOpcodeForClass(Class<? extends NetworkMessage> theClass){
		int code = networkMessageToIntMapper.getClassNumber(theClass);
		if(code < 0){
			Utils.fatal("Network message class: " + theClass.getName() + 
					" was never registered to DataTranslator.");
		}
		return (byte)code;
	}
	
	
	public static NetworkMessage readMessage(DataInputStream input){
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
				Utils.fatal("Unable to translate opcode: " + opcode + 
						". You might need to subscribe to it.");
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
	
	public static void writeMessage(NetworkMessage msg, DataOutputStream output){
		try {
			// First write the opcode
			output.writeByte(getOpcodeForClass(msg.getClass()));
			
			// Then use the NetworkMessage to write the rest of the data to output
			msg.writeData(output);
		} catch (IOException e) {
			Utils.err("Translator unable to write to socket");
		}
	}
	
}
