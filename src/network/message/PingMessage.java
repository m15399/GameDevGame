package network.message;


import game.Globals;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

public class PingMessage extends NetworkMessage {

	public boolean request;
	public long sentTime;
	public double gameTime;
	
	public PingMessage(){
		request = true;
		sentTime = new Date().getTime();
		gameTime = Globals.getNetworkGameTime();
	}
	
	public void readData(DataInputStream input) throws IOException {
		request = input.readBoolean();
		sentTime = input.readLong();
		gameTime = input.readDouble();
	}

	public void writeData(DataOutputStream output) throws IOException {
		output.writeBoolean(request);
		output.writeLong(sentTime);
		output.writeDouble(gameTime);
	}
}
