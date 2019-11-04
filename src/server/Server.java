package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {

	private int port;

	private List<User> clients;

	private TranslationService translationService;

	private ServerSocket server;

	public static void main(String[] args) throws IOException {
		new Server(9999).run();
	}

	private Server(int port) {
		this.port = port;
		this.clients = new ArrayList<>();
	}

	private void run() throws IOException {
		server = new ServerSocket(port) {
			protected void finalize() throws IOException {
				this.close();
			}
		};
		System.out.println("Port 9999 is now open.");

		while (true) {
			Socket client = server.accept();
			String nickname = (new Scanner(client.getInputStream())).nextLine();
			System.out.println("New Client: " + nickname + " Host:" + client.getInetAddress().getHostAddress());

			User newUser = new User(client, nickname);

			this.clients.add(newUser);
			newUser.getOutStream().println("Hi  " + newUser.getNickname());

			// create a new thread for newUser incoming messages handling
			new Thread(new UserHandler(this, newUser)).start();
		}
	}

	public void removeUser(User user) {
		this.clients.remove(user);
	}

	// send messages to all clients
	public void broadcastMessages(String msg, User userSender) {
		for (User client : this.clients) {
			client.getOutStream().println(userSender.getNickname() + ": " + msg);
		}
	}

	// send list of clients to all Users
	public void broadcastAllUsers() {
		for (User client : this.clients) {
			client.getOutStream().println(this.clients);
		}
	}
}