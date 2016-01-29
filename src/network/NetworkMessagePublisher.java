package network;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import utils.Observer;
import utils.Utils;


/**
 * Publish/subscribe system for network messages. You can subscribe an Observer 
 * object to updates from a specific class of NetworkMessage. 
 */
public class NetworkMessagePublisher {

	private HashMap<Class<? extends NetworkMessage>, Observer> subscribers;
	
	/**
	 * Should we forward messages when recieved, or hold until 
	 * forwardQueuedMessages() is called
	 */
	public boolean forwardImmediately; 
	
	private Queue<NetworkMessage> queuedMessages;
		
	public NetworkMessagePublisher(){
		subscribers = new HashMap<Class<? extends NetworkMessage>, Observer>();
		queuedMessages = new LinkedList<NetworkMessage>();
		forwardImmediately = false;
	}
	
	/**
	 * Take the message and forward it to subscribers
	 */
	public synchronized void takeMessage(NetworkMessage message){
		if(forwardImmediately)
			forward(message);
		else
			queuedMessages.add(message);
	}

	/**
	 * Subscribe the observer to get messages of a given class. Can
	 * only have one observer per class right now.
	 */
	public void subscribe(Class<? extends NetworkMessage> theClass, Observer observer){
		subscribers.put(theClass, observer);
	}
	
	private void forward(NetworkMessage msg){
		Observer observer = subscribers.get(msg.getClass());
		if(observer == null){
			Utils.err("No reciever for message: " + msg + 
					" - You should make sure to be subscribed to all possible messages!");
		} else {
			observer.notify(msg);
		}
	}
	
	/**
	 * Forward all queued messages to their subscribers
	 */
	public synchronized void forwardQueuedMessages(){
		for(NetworkMessage msg : queuedMessages){
			forward(msg);
		}
		queuedMessages.clear();
	}
	
}
