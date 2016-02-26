package network;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import network.message.NetworkMessage;

/**
 * Uses the DataTranslator to send and interpret NetworkMessage objects over a 
 * DataStream (stream of bytes, floats, UTF strings, etc)
 */
public class DataStreamHandler extends StreamHandler {
	
	private DataOutputStream dataOut;
	private BufferedOutputStream bufOut;
	private DataInputStream dataIn;
	
	public DataStreamHandler(SocketHandler socketHandler){
		super(socketHandler);
		
		// should never hit the 'size' param
		bufOut = new BufferedOutputStream(socketHandler.getOutputStream(), 4096);
		dataOut = new DataOutputStream(bufOut);
		dataIn = new DataInputStream(socketHandler.getInputStream());

	}
	
	public int sendMessage(NetworkMessage msg){
		try {
			DataTranslator.writeMessage(msg, dataOut);
			bufOut.flush();
			return 0;
		} catch (IOException e2) {
			return 1;
		}
	}
	
	public NetworkMessage readObject(){
		NetworkMessage msg = null;
		msg = DataTranslator.readMessage(dataIn);
		return msg;
	}
}
