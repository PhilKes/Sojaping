package server;

import client.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
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

			if (accountOrLoginAsJson.contains("aid")) { // register
				try {
					System.out.println("Try Register Account");
					this.registerUser(client, accountOrLoginAsJson);
				} catch (Exception e) {
					// Send to client e.getMessage
					e.printStackTrace();
					newUser.getOutStream().println(JsonHelper.getJsonOfObject(e));
				}

				newUser.getOutStream().println("Hi new registered user " + newUser.getNickname());
				// create a new thread for newUser incoming messages handling
				new Thread(new UserHandler(this, newUser)).start();
			} else {
				System.out.println("Try Login Account");
				//Login
				LoginUser loginUser = JsonHelper.convertJsonToLoginUser(accountOrLoginAsJson);
				//  TODO Check if loginUser exists in DB, else throw not authorizied exception
				newUser = new User(client, loginUser.getUserName());

				newUser.getOutStream().println("Hi welcome back  " + newUser.getNickname());
				// create a new thread for newUser incoming messages handling
				new Thread(new UserHandler(this, newUser)).start();
			}
		}
	}

	private Account convertJsonToAccount(String jsonInString) {
		ObjectMapper mapper = new ObjectMapper();
		Account account = null;
		try {
			// JSON string to Java object
			account = mapper.readValue(jsonInString, Account.class);

			// compact print
			System.out.println("Received Json from client: " + account);
			// pretty print
			String prettyStaff1 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(account);
			System.out.println(prettyStaff1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return account;
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
	private User registerUser(Socket client, String accountOrLoginAsJson) throws Exception {
		Account account = JsonHelper.convertJsonToAccount(accountOrLoginAsJson);

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