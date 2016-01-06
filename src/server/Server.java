package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import network.NetworkMessagePublisher;
import network.SocketHandler;
import network.TestMessage;

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

	public Server(int port) {
		this.port = port;
		
		serverPub = new NetworkMessagePublisher();
		serverPub.forwardImmediately = true;
		
		serverPub.subscribe(TestMessage.class, new Observer(){
			public void notify(Object arg){
				TestMessage msg = (TestMessage) arg;
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
				SocketHandler handler = new SocketHandler(sock, serverPub);
				new Thread(handler).start();
				
				// Test message
				handler.sendMessage(new TestMessage("Hello from server!", 3.14159f));
				
			} catch (IOException e) {
				Utils.err("Failed to accept socket");
			}
		}
	}
}
