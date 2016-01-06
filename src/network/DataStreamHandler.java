package network;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import engine.Utils;

/**
 * Uses the DataTranslator to send and interpret NetworkMessage objects over a 
 * DataStream (stream of bytes, floats, UTF strings, etc)
 */
public class DataStreamHandler extends StreamHandler {
	
	private DataOutputStream dataOut;
	private BufferedOutputStream bufOut;
	private DataInputStream dataIn;
	
	public DataStreamHandler(SocketHandler socketHandler, DataTranslator translator){
		super(socketHandler, translator);
		
		bufOut = new BufferedOutputStream(socketHandler.getOutputStream());
		dataOut = new DataOutputStream(bufOut);
		dataIn = new DataInputStream(socketHandler.getInputStream());

	}
	
	public void sendMessage(NetworkMessage msg){
		try {
			getTranslator().writeMessage(msg, dataOut);
			bufOut.flush();
		} catch (IOException e2) {
			Utils.err("Unable to send message");
		}
	}
	
	public NetworkMessage readObject(){
		NetworkMessage msg = null;
		msg = getTranslator().readMessage(dataIn);
		return msg;
	}
}
