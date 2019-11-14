package server;

import client.LoginUser;
import common.data.Account;
import common.data.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static common.Constants.Contexts.*;
import static common.JsonHelper.*;

public class Server {

	//public static String SERVER_HOST = "141.59.129.236";
	public static String SERVER_HOST = "141.59.130.180";

	public static int SERVER_PORT = 443;

	private int port;

	private List<Connection> clients;

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
			/** Receive connectPacket with Clients IP as data, add to client list*/
            Packet connectPacket =getPacketFromJson(new Scanner(clientSocket.getInputStream()).nextLine());
            if(connectPacket.getContext().equals(CONNECT)) {
                String clientIP=(String) connectPacket.getData();
                System.out.println("New Client: " + clientIP + " Nickname: " + clientIP);
                //TODO Try to reconnect when connection failed
                Connection newConnection= new Connection(clientSocket, clientSocket.getInetAddress().getHostAddress());
                this.clients.add(newConnection);
                sendToUser(newConnection,CONNECT_SUCCESS,"Hello "+clientIP);
                /** Start Handler for the new client*/
                new Thread(new ServerHandler(this, newConnection)).start();
            }
		}
	}

	/** Checks if loginUser has valid credentials, returns Account from DB
     *  throws Exception if invalid credentials */
	public Account loginUser(Connection newConnection, LoginUser loginUser) throws Exception {
		//LoginUser loginUser = JsonHelper.convertJsonToObject(accountOrLoginAsJson);
		Account account = this.dbService.getAccountByLoginUser(loginUser);
		if (account != null) {
			if (account.getPassword().equals(loginUser.getPassword()))
                return account;
			else
				throw new Exception("Invalid password");
		} else
			throw new Exception("Unknown username");
	}

	public void removeUser(Connection connection) {
		this.clients.remove(connection);
	}

	// send messages to all clients
	public void broadcastMessages(String msg, Connection connectionSender) {
		for (Connection client : this.clients) {
			sendToUser(client,BRDCAST_MSG, connectionSender.getNickname() + ": " + msg);
		}
	}

	//TODO return filled Account from DB
	public void registerUser(Socket client, Account account) throws Exception {
		this.dbService.insert(account);
	}

	// send list of clients to all Users
	public void broadcastAllUsers() {
		for (Connection client : this.clients) {
			sendToUser(client,BRDCAST_USERS,this.clients);
		}
	}

	/** Send object as JSON through user OutputStream*/
	public void sendToUser(Connection connection, String context, Object object){
        Packet packet=new Packet(context,object);
		connection.getOutStream().println(packet.getJson());
        System.out.println("to "+connection.getNickname()+"\t:\t"+packet);
	}
}