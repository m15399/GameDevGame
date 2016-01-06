package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import network.DataTranslator;
import network.NetworkMessagePublisher;
import network.SocketHandler;
import network.TestNetworkMessage;

import engine.Observer;
import engine.Utils;
import game.GameDevGame;

/**
 * Server main class - runs the Server.
 */
public class Server implements Runnable {

	public static void main(String[] args){
		Server server = new Server(8000);
		new Thread(server).start();
		
		// Start a game window too (makes testing easier)
		GameDevGame.main(null);
	}
	
	
	public int port;
	private ServerSocket serverSocket;
	
	private NetworkMessagePublisher serverPub;
	private DataTranslator translator;

	public Server(int port) {
		this.port = port;
		
		translator = new DataTranslator();
		serverPub = new NetworkMessagePublisher(translator);
		serverPub.forwardImmediately = true;
		
		serverPub.subscribe(TestNetworkMessage.class, new Observer(){
			public void notify(Object arg){
				TestNetworkMessage msg = (TestNetworkMessage) arg;
				System.out.println("Server recieved message: " + msg);
			}
		});
		
		
		System.out.println("Starting server on port: " + port);
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			Utils.err("Failed to create server socket");
			return;
		}
	}
	
	public void run() {
		
		// Listen for new connections
		while (true) {
			Socket sock;
			try {
				sock = serverSocket.accept();
				System.out.println("A user connected");

				// Start a handler for each user
				SocketHandler handler = new SocketHandler(sock, serverPub, translator);
				new Thread(handler).start();
				
				// Test message
				handler.sendMessage(new TestNetworkMessage("Hello from server!", 3.14159f));
				
			} catch (IOException e) {
				Utils.err("Failed to accept socket");
			}
		}
	}
}
