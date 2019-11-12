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
import static common.JsonHelper.convertObjectToJson;

public class Server {

	//public static String SERVER_HOST = "141.59.129.236";
	public static String SERVER_HOST = "192.168.178.26";

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
		System.out.println("Host " + inetAddress + " port: " + port + " is now open.");

		/** Continouesly accept new Socket connections, instanstiate new UserHandler for every new connection (newUser)*/
		while (true) {
			Socket clientSocket = server.accept();

			String clientIP = convertJsonToObject(new Scanner(clientSocket.getInputStream()).nextLine());
			// Hier kriegen wir ein JSON von Account User + PW oder Login
			System.out.println("New Client: " + clientIP + " Nickname: " + clientIP);

			User newUser = new User(clientSocket, clientSocket.getInetAddress().getHostAddress());
			this.clients.add(newUser);
			sendToUser(newUser,"Hello "+clientIP);
			// create a new thread for newUser incoming messages handling
			new Thread(new UserHandler(this, newUser)).start();
		}
	}

	public void loginUser(final Socket client, final String accountOrLoginAsJson, User newUser) throws IOException {
		LoginUser loginUser = JsonHelper.convertJsonToObject(accountOrLoginAsJson);
		Account account = this.dbService.getAccountByLoginUser(loginUser);
		if (account != null) {
			if (account.getPassword().equals(loginUser.getPassword())) {
				// successful auth
				sendToUser(newUser,account);

				newUser = new User(client, loginUser.getUserName());

				sendToUser(newUser,"Hi welcome back  " + newUser.getNickname());
				// create a new thread for newUser incoming messages handling
				new Thread(new UserHandler(this, newUser)).start();
			} else {
				Exception e = new Exception("Invalid password");
				sendToUser(newUser,e);
			}
		} else {
			Exception e = new Exception("Unknown username");
			sendToUser(newUser,e);
		}
	}

	public void removeUser(User user) {
		this.clients.remove(user);
	}

	// send messages to all clients
	public void broadcastMessages(String msg, User userSender) {
		for (User client : this.clients) {
			sendToUser(client,userSender.getNickname() + ": " + msg);
		}
	}
	public void registerUser(Socket client, Account account) throws Exception {
		this.dbService.insert(account);
		//return new User(client, account.getUserName());
		return;
	}
	// send list of clients to all Users
	public void broadcastAllUsers() {
		for (User client : this.clients) {
			sendToUser(client,this.clients);
		}
	}
	/** Send object as JSON through user OutputStream*/
	public void sendToUser(User user, Object object){
		String json=convertObjectToJson(object);
		user.getOutStream().println(json);
	}
}