package client;

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
		//		new Client("141.59.135.57", 443).run();
		getInstance(Server.SERVER_HOST, Server.SERVER_PORT).run();
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
		output.println(getJsonOfObject(registerAccount));
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

	private String getJsonOfObject (Object object){

		ObjectMapper mapper = new ObjectMapper();
		String jsonString = "";

		try {

			// Java objects to JSON string - compact-print
			jsonString = mapper.writeValueAsString(object);

			System.out.println(jsonString);

			// Java objects to JSON string - pretty-print
			String jsonInString2 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);

			System.out.println(jsonInString2);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return jsonString;
	}
}

