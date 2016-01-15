package game;

import java.util.HashMap;

import network.Client;
import network.PlayerDisconnectMessage;
import network.PlayerUpdateMessage;
import engine.GameObject;
import engine.Observer;

/**
 * Manages the players on the map. Primarily handles network updates. 
 * Forwards messages to the right player, creates/destroys players when necessary
 */
public class PlayerManager extends GameObject {
	
	// Maps playerNumbers to players
	private HashMap<Short, Player> players;
	
	public PlayerManager(){
		
		Client.publisher.subscribe(PlayerUpdateMessage.class, new Observer(){
			public void notify(Object arg){
				PlayerUpdateMessage msg = (PlayerUpdateMessage) arg;
				processUpdateMessage(msg);
			}
		});
		Client.publisher.subscribe(PlayerDisconnectMessage.class, new Observer(){
			public void notify(Object arg){
				PlayerDisconnectMessage msg = (PlayerDisconnectMessage) arg;
				destroyAndRemovePlayer(msg.playerNumber);
			}
		});
		
		players = new HashMap<Short, Player>();
	}
	
	private void processUpdateMessage(PlayerUpdateMessage msg){
		Player p = players.get(msg.playerNumber);
		
		if(p == null){ 
			// Got an update from a player we don't have yet,
			// so create a new player 
			p = new Player(msg.playerNumber);
			p.makeDummy();
		}
		// Send the update to the Player
		p.recieveUpdateFromServer(msg);
	}
	
	public void addPlayer(Player p){
		players.put(p.getPlayerNumber(), p);
	}
	
	public void removePlayer(Player p){
		players.remove(p.getPlayerNumber());
	}
	
	private void destroyAndRemovePlayer(short num){
		Player removed = players.remove(num);
		if(removed != null && removed.isDummy()){
			removed.destroy();
		}
	}
	
}
