package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import utils.Utils;

import game.Tile;
import game.Tile.Type;

public class TileUpdatesMessage extends NetworkMessage {
	
	// Helper methods for translating tiles into bytes

	public static byte toByte(Type type, int heat){
		if(heat >= (2 << 5) - 1){
			// 11111 is -1, so we can't send 31
			Utils.fatal("Cannot send a heat value >= 31, takes too many bits");
		}
		int typeInt = type.ordinal();
		if(typeInt >= 2 << 3){
			Utils.fatal("Cannot write tile type >= 8 in 3 bits");
		}

		byte dataByte = (byte)((typeInt << 5) | (heat & 0x1F));
	
		return dataByte;
	}
	
	public static Type getType(byte dataByte){
		int typeInt = ((int)(dataByte & 0xE0)) >> 5;
		return Type.values()[typeInt];
	}

	public static int getHeat(byte dataByte){
		int heat = dataByte & 0x1F;
		if(heat == 0x1F)
			heat = -1;
		return heat;
	}
	
	
	
	public ArrayList<Integer> xCoords;
	public ArrayList<Integer> yCoords;
	public ArrayList<Tile.Type> types;
	public ArrayList<Integer> heats;
	
	public TileUpdatesMessage(){
		xCoords = new ArrayList<Integer>();
		yCoords = new ArrayList<Integer>();
		types = new ArrayList<Tile.Type>();
		heats = new ArrayList<Integer>();
	}

	/**
	 * Add an entry to be sent in the message
	 */
	public void addUpdate(int tx, int ty, Type type, int heat){
		xCoords.add(tx);
		yCoords.add(ty);
		types.add(type);
		heats.add(heat);
	}
	
	public void readData(DataInputStream input) throws IOException {
		int size = input.readByte() & 0xFF;
		for(int i = 0; i < size; i++){
			xCoords.add(input.readByte() & 0xFF);
			yCoords.add(input.readByte() & 0xFF);
			
			byte dataByte = input.readByte();
			
			types.add(getType(dataByte));
			heats.add(getHeat(dataByte));
		}
	}

	public void writeData(DataOutputStream output) throws IOException {
		if(xCoords.size() > Byte.MAX_VALUE){
			Utils.fatal("Trying to send too many tile updates! Cannot write this many");
		}
		byte size = (byte)xCoords.size();
		output.writeByte(size);
		
		for(int i = 0; i < size; i++){
			output.writeByte(xCoords.get(i).byteValue());
			output.writeByte(yCoords.get(i).byteValue());
			
			output.writeByte(toByte(types.get(i), heats.get(i)));
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
