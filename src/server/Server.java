package server;

import client.LoginUser;
import common.JsonHelper;
import common.data.Account;

import java.io.IOException;
import java.net.InetAddress;
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

	private DatabaseService dbService;

	private Server(int port) {
		this.port = port;
		this.clients = new ArrayList<>();
		this.dbService = new DatabaseService();
	}
	public static void main(String[] args) throws IOException {
		new Server(9900).run();
		//		new Server(443).run();
	}
	private void run() throws IOException {
		String inetAddress = InetAddress.getLocalHost().getHostAddress();

		server = new ServerSocket(port, 50, InetAddress.getByName(inetAddress)) {
			protected void finalize() throws IOException {
				this.close();
			}
		};
		//server.bind(new InetSocketAddress(" 141.59.135.57", 443));
		System.out.println("Host " + inetAddress + " port:" + port + "is now open.");

		while (true) {
			Socket client = server.accept();

			String accountOrLoginAsJson = (new Scanner(client.getInputStream())).nextLine();
			// Hier kriegen wir ein JSON von Account User + PW oder Login

			System.out.println("New Client: " + accountOrLoginAsJson + " Host:" + client.getInetAddress().getHostAddress());

			User newUser = null;

			if (accountOrLoginAsJson.contains("aid")) { // register
				try {
					this.registerUser(client, accountOrLoginAsJson);
				} catch (Exception e) {
					// Send to client e.getMessage
					e.printStackTrace();
				}
			} else {
				//Login
				LoginUser loginUser = JsonHelper.convertJsonToLoginUser(accountOrLoginAsJson);
				// Check if loginUser exists in DB
				newUser = new User(client, loginUser.getUserName());
			}

			if(newUser !=null){
				this.clients.add(newUser);
				newUser.getOutStream().println("Hi  " + newUser.getNickname());

				// create a new thread for newUser incoming messages handling
				new Thread(new UserHandler(this, newUser)).start();
			}
		}
	}

	private User registerUser(Socket client, String accountOrLoginAsJson) throws Exception {
		Account account = JsonHelper.convertJsonToAccount(accountOrLoginAsJson);

		this.dbService.insert(account);

		return new User(client, account.getUserName());
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