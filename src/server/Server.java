package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.data.Account;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
	public static String SERVER_HOST="141.59.129.236";
	public static int SERVER_PORT=443;

	private int port;

	private List<User> clients;

	private TranslationService translationService;

	private ServerSocket server;

	private DatabaseService dbService;
	public static void main(String[] args) throws IOException {
		new Server(SERVER_PORT).run();
	}

	private Server(int port) {
		this.port = port;
		this.clients = new ArrayList<>();
		this.dbService = new DatabaseService();
	}

	private void run() throws IOException {
		String inetAddress = InetAddress.getLocalHost().getHostAddress();

		server = new ServerSocket(port, 50, InetAddress.getByName(inetAddress)) {
			protected void finalize() throws IOException {
				this.close();
			}
		};
		//server.bind(new InetSocketAddress(" 141.59.135.57", 443));
		System.out.println("Host "+ inetAddress + " port:"+ port + "is now open.");

		while (true) {
			Socket client = server.accept();
			String accountAsJson = (new Scanner(client.getInputStream())).nextLine();
			// Hier kriegen wir ein JSON von Account User + PW

			System.out.println("New Client: " + accountAsJson + " Host:" + client.getInetAddress().getHostAddress());

			Account account = this.convertJsonToAccount(accountAsJson);

			// check if account exist in DB
			// if not create new
			// else get existing

			this.dbService.insert(account);

			User newUser = new User(client, account.getUserName());

			this.clients.add(newUser);
			newUser.getOutStream().println("Hi  " + newUser.getNickname());

			// create a new thread for newUser incoming messages handling
			new Thread(new UserHandler(this, newUser)).start();
		}
	}

	private void loginOrRegisterAccount() {

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

	// send list of clients to all Users
	public void broadcastAllUsers() {
		for (User client : this.clients) {
			client.getOutStream().println(this.clients);
		}
	}
}