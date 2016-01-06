package network;

import java.io.Serializable;

public class TestMessage extends NetworkMessage implements Serializable{

	private static final long serialVersionUID = -6125001571418567863L;

	public String greeting;
	public float someNumber;
	
	public TestMessage(String s, float f){
		greeting = s;
		someNumber = f;
	}
	
	public String toString(){
		return greeting + " ; " + someNumber;
	}
}
