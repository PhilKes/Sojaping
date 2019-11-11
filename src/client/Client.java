package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.JsonHelper;
import common.data.Account;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {

	private LoginUser loginUser;

	private Account account;

	private String host;

	private int port;

	public Client(String host, int port) {
		this.host = host;
		this.port = port;
		//this.loginUser = new LoginUser();
	}
	public static void main(String[] args) throws IOException {
		new Client("10.0.75.1", 9900).run();
		//		new Client("141.59.135.57", 443).run();
	}
	public void run() throws IOException {
		Socket client = new Socket(host, port);
		System.out.println("Client successfully connected to server!");

		PrintStream output = new PrintStream(client.getOutputStream());

		Scanner sc = new Scanner(System.in);
		System.out.print("Enter your name: ");

		Account registerAccount = new Account();
		registerAccount.setUserName(sc.nextLine());


		System.out.print("Enter your password: ");
		registerAccount.setPassword(sc.nextLine());

		//this.loginUser.setUserName(sc.nextLine());

		//send to server
		output.println(JsonHelper.getJsonOfObject(registerAccount));
//		output.println(loginUser.getUserName());

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

