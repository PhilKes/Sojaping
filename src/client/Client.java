package client;

import common.data.Account;
import server.Server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {

	private static Client client;
	private LoginUser loginUser;
	private Account account;

	private String host;

	private int port;

	public static Client getInstance(String host, int port) {
		if(client==null)
			client=new Client(host,port);
		return client;
	}
	private Client(String host,int port){
		this.host = host;
		this.port = port;
		//this.loginUser = new LoginUser();
	}
	public static void main(String[] args) throws IOException {
		getInstance(Server.SERVER_HOST, Server.SERVER_PORT).run();
	}
	public void run() throws IOException {
		Socket client = new Socket(host, port);
		System.out.println("Client successfully connected to server!");

		PrintStream output = new PrintStream(client.getOutputStream());

		Scanner sc = new Scanner(System.in);
		System.out.print("Enter your name: ");

		this.loginUser.setUserName(sc.nextLine());

		//send to server
		output.println(loginUser.getUserName());

		// create a new thread for server messages handling
		new Thread(new ReceivedMessagesHandler(client.getInputStream())).start();

		System.out.println("Messages: \n");

		// Read new messages and send them to server
		while (sc.hasNextLine()) {
			output.println(sc.nextLine());
		}

		output.close();
		sc.close();
		client.close();
	}
}

