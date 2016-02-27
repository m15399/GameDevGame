package network.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import utils.Utils;


/**
 * Contains info about the state of a player. Position, velocity, etc
 */
public class PlayerUpdateMessage extends NetworkMessage {
	
	public String name;
	public short playerNumber;
	public double x, y;
	public double vx, vy;
	public double inputX, inputY;
	public double angle;
	public double jumpTime, fallTime;
	public double time = -1;
	public boolean firing, jumping, falling;
	// weapon info
	
	
	public PlayerUpdateMessage(){}
	
	public PlayerUpdateMessage(short playerNumber, double x, double y, double vx, double vy,
			double inputX, double inputY, double angle, double jumpTime, double fallTime,
			boolean firing, boolean jumping, boolean falling){
		this.playerNumber = playerNumber;
		this.x = x;
		this.y = y;
		this.vx = vx;
		this.vy = vy;
		this.inputX = inputX;
		this.inputY = inputY;
		this.angle = angle;
		this.jumpTime = jumpTime;
		this.fallTime = fallTime;
		this.firing = firing;
		this.jumping = jumping;
		this.falling = falling;
		name = "";
		
	}
	
	public String toString(){
		return "Player #" + playerNumber + ": (" + x + ", " + y + ")";
	}

	private byte inToByte(double in){
		byte b = Utils.packRange(in, -1, 1, 8);
		return b;
	}
	
	private double byteToIn(byte b){
		double in = Utils.unpackRange(b, -1, 1, 8);
		if(Math.abs(in) < 0.01){
			in = 0;
		}
		return in;
	}
	
	public void readData(DataInputStream input) throws IOException {
		name = input.readUTF();
		playerNumber = input.readShort();
		x = input.readFloat();	
		y = input.readFloat();	
		vx = input.readFloat();
		vy = input.readFloat();
		inputX = byteToIn(input.readByte());
		inputY = byteToIn(input.readByte());
		
		// This byte contains - firing bit/jumping bit/falling bit/flame angle 5 bits
		byte angleAndData = input.readByte();
		firing = (angleAndData & (1 << 7)) != 0;
		jumping = (angleAndData & (1 << 6)) != 0;
		falling = (angleAndData & (1 << 5)) != 0;
		
		byte angleByte = (byte)(angleAndData & 0x1F);
		angle = Utils.unpackRange(angleByte, -180, 180, 5);
		
		jumpTime = input.readFloat();
		fallTime = input.readFloat();
		
		time = input.readDouble();
	}

	public void writeData(DataOutputStream output) throws IOException {
		output.writeUTF(name);
		output.writeShort(playerNumber);
		output.writeFloat((float)x);
		output.writeFloat((float)y);
		output.writeFloat((float)vx);
		output.writeFloat((float)vy);
		output.writeByte(inToByte(inputX));
		output.writeByte(inToByte(inputY));

		// Pack this data - firing bit/jumping bit/falling bit/flame angle 5 bits
		byte angle5bit = Utils.packRange(angle, -180, 180, 5);
		byte fireBit = (byte)(firing ? 1 << 7 : 0);
		byte jumpBit = (byte)(jumping ? 1 << 6 : 0);
		byte fallBit = (byte)(falling ? 1 << 5 : 0);
		byte angleAndData = (byte)(angle5bit | fireBit | jumpBit | fallBit);
		output.writeByte(angleAndData);
		
		output.writeFloat((float) jumpTime);
		output.writeFloat((float) fallTime);
		
		output.writeDouble(time);
	}
}
