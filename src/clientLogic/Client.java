package clientLogic;

import data.Account;

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
		this.loginUser = new LoginUser();
	}
	public static void main(String[] args) throws IOException {
		new Client("127.0.0.1", 9999).run();
	}
	public void run() throws IOException {
		Socket client = new Socket(host, port);
		System.out.println("Client successfully connected to server!");

		PrintStream output = new PrintStream(client.getOutputStream());

		Scanner sc = new Scanner(System.in);
		System.out.print("Enter your name: ");

		this.loginUser.setName(sc.nextLine());

		//send to server
		output.println(loginUser.getName());

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

