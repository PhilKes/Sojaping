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

import static common.JsonHelper.convertJsonToObject;
import static common.JsonHelper.getJsonOfObject;

public class Server {

	public static String SERVER_HOST = "141.59.129.236";

	public static int SERVER_PORT = 443;

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
		new Server(SERVER_PORT).run();
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

			/*String accountAsJson = (new Scanner(client.getInputStream())).nextLine();*/
			// Hier kriegen wir ein JSON von Account User + PW

			String accountOrLoginAsJson = (new Scanner(client.getInputStream())).nextLine();
			// Hier kriegen wir ein JSON von Account User + PW oder Login

			System.out.println("New Client: " + accountOrLoginAsJson + " Host:" + client.getInetAddress().getHostAddress());

			User newUser = new User(client, "Guest");
			this.clients.add(newUser);
			Object receivedObj=convertJsonToObject(accountOrLoginAsJson);
			/** Determine type of object sent*/

			if (receivedObj instanceof Account) { // register
				try {
					System.out.println("Try Register Account");
					Account account = (Account) receivedObj;
					this.registerUser(client, account);
				} catch (Exception e) {
					newUser.getOutStream().println(JsonHelper.getJsonOfObject(e));
				}
				newUser.getOutStream().println(getJsonOfObject("Hi new registered user " + newUser.getNickname()));

				// create a new thread for newUser incoming messages handling
				new Thread(new UserHandler(this, newUser)).start();
			} else if(receivedObj instanceof LoginUser) {
				System.out.println("Try Login Account");
				loginUser(client, accountOrLoginAsJson, newUser);
			}
		}
	}

	private void loginUser(final Socket client, final String accountOrLoginAsJson, User newUser) throws IOException {
		LoginUser loginUser = JsonHelper.convertJsonToObject(accountOrLoginAsJson);

		Account account = this.dbService.getAccountByLoginUser(loginUser);
		if (account != null) {
			if (account.getPassword().equals(loginUser.getPassword())) {
				// successful auth
				newUser.getOutStream().println(JsonHelper.getJsonOfObject(account));

				newUser = new User(client, loginUser.getUserName());

				newUser.getOutStream().println("Hi welcome back  " + newUser.getNickname());
				// create a new thread for newUser incoming messages handling
				new Thread(new UserHandler(this, newUser)).start();
			} else {
				Exception e = new Exception("Invalid password");
				newUser.getOutStream().println(JsonHelper.getJsonOfObject(e));
			}
		} else {
			Exception e = new Exception("Unknown username");
			newUser.getOutStream().println(JsonHelper.getJsonOfObject(e));
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
	private User registerUser(Socket client, Account account) throws Exception {

		this.dbService.insert(account);
		return new User(client, account.getUserName());
	}
	// send list of clients to all Users
	public void broadcastAllUsers() {
		for (User client : this.clients) {
			client.getOutStream().println(this.clients);
		}
	}
}