package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import engine.Utils;

/**
 * Sends updates about tiles which have had their heats changed. The format is:
 * N, tx1, ty1, heat1, tx2, ty2, heat2, ... , txN, tyN, heatN.
 * (All bytes. tx is tile x)
 */
public class TileHeatUpdatesMessage extends NetworkMessage {

	private static final long serialVersionUID = -1339144518186379042L;
	
	public ArrayList<Integer> xCoords;
	public ArrayList<Integer> yCoords;
	public ArrayList<Integer> heats;
	
	public TileHeatUpdatesMessage(){
		xCoords = new ArrayList<Integer>();
		yCoords = new ArrayList<Integer>();
		heats = new ArrayList<Integer>();
	}
	
	/**
	 * Add an entry of tileX, tileY, heat to be sent in the message
	 */
	public void addHeat(int tx, int ty, double heat){
		xCoords.add(tx);
		yCoords.add(ty);
		heats.add((int)Math.floor(heat));
	}
	
	@Override
	public OpCode getOpcode() {
		return OpCode.TILE_HEAT_UPDATES;
	}

	@Override
	public void readData(DataInputStream input) throws IOException {
		int size = input.readByte() & 0xFF;
		for(int i = 0; i < size; i++){
			xCoords.add(input.readByte() & 0xFF);
			yCoords.add(input.readByte() & 0xFF);
			heats.add(input.readByte() & 0xFF);			
		}
	}

	@Override
	public void writeData(DataOutputStream output) throws IOException {
		if(xCoords.size() > Byte.MAX_VALUE){
			Utils.fatal("Trying to send too many tile heats! Cannot write this many");
		}
		byte size = (byte)xCoords.size();
		output.write(size);
		
		for(int i = 0; i < size; i++){
			output.writeByte(xCoords.get(i).byteValue());
			output.writeByte(yCoords.get(i).byteValue());
			
			int heat = heats.get(i);
			if(heat >= 64){
				Utils.fatal("Cannot send a heat value this high, takes too many bits");
			}
			output.writeByte((byte)heat);
		}
	}

	public String toString(){
		String s = "";
		for(int i = 0; i < xCoords.size(); i++){
			s += xCoords.get(i) + ", " + yCoords.get(i) + " = " + heats.get(i) + "\n";
		}
		return s;
	}
	
}
