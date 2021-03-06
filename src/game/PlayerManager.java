package game;

import java.util.Collection;
import java.util.HashMap;

import utils.Observer;

import network.message.PlayerDisconnectMessage;
import network.message.PlayerInputMessage;
import network.message.PlayerUpdateMessage;
import engine.GameObject;

/**
 * Manages the players on the map. Primarily handles network updates. 
 * Forwards messages to the right player, creates/destroys players when necessary
 */
public class PlayerManager extends GameObject {
	
	// TODO on disconnect, ban that number
	
	// Maps playerNumbers to players
	private HashMap<Short, Player> players;
	
	public PlayerManager(){
		
		if(!Globals.isServer()){
			Globals.publisher().subscribe(PlayerUpdateMessage.class, new Observer(){
				public void notify(Object arg){
					PlayerUpdateMessage msg = (PlayerUpdateMessage) arg;
					Player p = findOrCreatePlayer(msg.playerNumber);
					if(p != null){
						p.recieveUpdateFromServer(msg);
					}
				}
			});
			Globals.publisher().subscribe(PlayerDisconnectMessage.class, new Observer(){
				public void notify(Object arg){
					PlayerDisconnectMessage msg = (PlayerDisconnectMessage) arg;
					destroyAndRemovePlayer(msg.playerNumber);
				}
			});
		} else {
			// Server
			
			Globals.publisher().subscribe(PlayerInputMessage.class, new Observer(){
				public void notify(Object arg){
					PlayerInputMessage msg = (PlayerInputMessage) arg;
					Player p = findOrCreatePlayer(msg.playerNumber);
					if(p != null)
						p.receieveInputs(msg);
					}
			});
		}
				
		players = new HashMap<Short, Player>();
	}
	
	private Player findOrCreatePlayer(short pNum){
		if(pNum < 0){
			return null;
		}
		
		Player p = players.get(pNum);
		
		if(p == null){
			// Got an update from a player we don't have yet,
			// so create a new player 
			p = new Player(pNum);
			p.makeDummy();
		}
		
		return p;
	}
	
	public Collection<Player> getPlayers(){
		return players.values();
	}
	
	public void addPlayer(Player p){
		players.put(p.getPlayerNumber(), p);
	}
	
	public void removePlayer(Player p){
		players.remove(p.getPlayerNumber());
	}
	
	public void destroyAndRemovePlayer(short num){
		Player removed = players.remove(num);
		if(removed != null && removed.isDummy()){
			removed.destroy();
		}
	}
	
}
