package client;

import common.data.Account;
import server.Server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

import static common.JsonHelper.convertObjectToJson;

public class Client {

	private static Client instance;
	private LoginUser loginUser;

	private Account account;

	private String host;
	private Socket client;
	private int port;
	private PrintStream output;

	public static Client getInstance(String host, int port) {
		if(instance ==null)
			instance =new Client(host,port);
		return instance;
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
		client = new Socket(host, port);
		System.out.println("Client successfully connected to server!");
		output = new PrintStream(client.getOutputStream());
		sendToServer(client.getInetAddress().getHostAddress());
		/*
		Scanner sc = new Scanner(System.in);
		System.out.print("Enter your name: ");
		Account registerAccount = new AccountBuilder().setUserName(sc.nextLine()).createAccount();

		System.out.print("Enter your password: ");
		registerAccount.setPassword(sc.nextLine());

		//this.loginUser.setUserName(sc.nextLine());

		//send to server
		output.println(JsonHelper.getJsonOfObject(registerAccount));
//		output.println(loginUser.getUserName());

		// create a new thread for server messages handling
		new Thread(new ReceivedMessagesHandler(client.getInputStream())).start();

		System.out.println("Messages: \n");*/

		// Read new messages and send them to server
		/*while (sc.hasNextLine()) {
			output.println(sc.nextLine());
		}*/

		//output.close();
		//sc.close();
		//client.close();
	}
	public void stop(){
		output.close();
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Send Object as JSON to Server*/
	public void sendToServer(Object object){
		output.println(convertObjectToJson(object));
		try {
			new Thread(new ReceivedMessagesHandler(client.getInputStream())).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

