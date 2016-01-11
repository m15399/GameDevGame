package network;

/**
 * Abstract base class for handling streams
 */
public abstract class StreamHandler {

	private SocketHandler socketHandler;
	private DataTranslator translator;

	public StreamHandler(SocketHandler socketHandler, DataTranslator translator){
		this.socketHandler = socketHandler;
		this.translator = translator;
	}
	
	public SocketHandler getSocketHandler(){
		return socketHandler;
	}
	
	public DataTranslator getTranslator(){
		return translator;
	}
	
	/**
	 * Send a message over the socket. Returns 0 if successful, else returns 1
	 */
	public abstract int sendMessage(NetworkMessage msg);

	/**
	 * Read a message from the socket
	 */
	public abstract NetworkMessage readObject();
}
