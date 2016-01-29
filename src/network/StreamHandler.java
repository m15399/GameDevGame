package network;

/**
 * Abstract base class for handling streams
 */
public abstract class StreamHandler {

	private SocketHandler socketHandler;

	public StreamHandler(SocketHandler socketHandler){
		this.socketHandler = socketHandler;
	}
	
	public SocketHandler getSocketHandler(){
		return socketHandler;
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
