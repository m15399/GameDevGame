package network.message;

import game.Globals;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerInputMessage extends NetworkMessage {

	public double x, y, aim;
	public boolean fire, weapon, jump;
	public short playerNumber;
	
	public PlayerInputMessage(){}
	
	public PlayerInputMessage(double x, double y, double aim, boolean fire, boolean weapon, 
			boolean jump){
		this.x = x;
		this.y = y;
		this.aim = aim;
		this.fire = fire;
		this.weapon = weapon;
		this.jump = jump;
		if(Globals.player != null){
			playerNumber = Globals.player.getPlayerNumber();
		} 
	}
	
	@Override
	public void readData(DataInputStream input) throws IOException {
		x = input.readDouble();
		y = input.readDouble();
		aim = input.readDouble();
		fire = input.readBoolean();
		weapon = input.readBoolean();
		jump = input.readBoolean();
		playerNumber = input.readShort();
	}

	@Override
	public void writeData(DataOutputStream output) throws IOException {
		output.writeDouble(x);
		output.writeDouble(y);
		output.writeDouble(aim);
		output.writeBoolean(fire);
		output.writeBoolean(weapon);
		output.writeBoolean(jump);	
		output.writeShort(playerNumber);
	}

}
