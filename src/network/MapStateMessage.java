package network;

import game.Globals;
import game.Tile;
import game.Tile.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Sends the entire map state. Sent to players who just joined
 */
public class MapStateMessage extends NetworkMessage {
	
	public ArrayList<Tile.Type> types;
	public ArrayList<Integer> heats;
	
	public MapStateMessage(){
		types = new ArrayList<Tile.Type>();
		heats = new ArrayList<Integer>();
	}
	
	public void readData(DataInputStream input) throws IOException {
		// will eventually depend on which map
		int n = Globals.map.getNumTiles();
		
		for(int i = 0; i < n; i++){
			byte dataByte = input.readByte();
			int heat = TileUpdatesMessage.getHeat(dataByte);
			Type type = TileUpdatesMessage.getType(dataByte);
			
			types.add(type);
			heats.add(heat);
		}
	}

	public void writeData(DataOutputStream output) throws IOException {
		for(int i = 0; i < types.size(); i++){
			byte dataByte = TileUpdatesMessage.toByte(types.get(i), heats.get(i));
			output.writeByte(dataByte);
		}
	}
	
}
