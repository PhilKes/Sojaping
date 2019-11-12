package server;

import client.LoginUser;
import common.data.Account;

import java.io.IOException;
import java.util.Scanner;

import static common.JsonHelper.convertJsonToObject;

public class UserHandler implements Runnable {

	private Server server;

	private User user;
	private boolean running;

	public UserHandler(Server server, User user) {
		this.server = server;
		this.user = user;
		this.running=true;
		//this.server.broadcastAllUsers();
	}

	/** Handles all incoming messages from user InputStream*/
	public void run() {
		Scanner sc = new Scanner(this.user.getInputStream());
		/*while (sc.hasNextLine()) {
			message = sc.nextLine();
			server.broadcastMessages(message, user);
		}*/
		while(running && sc.hasNextLine()) {
			String accountOrLoginAsJson=sc.nextLine();
			Object receivedObj=convertJsonToObject(accountOrLoginAsJson);
			/** Determine type of object sent*/
			/** Try to register if Account was sent*/
			if(receivedObj instanceof Account) {
				try {
					System.out.println("Try Register Account");
					Account account=(Account) receivedObj;
					server.registerUser(user.getSocket(), account);
					server.sendToUser(user,"Hi new registered user " + user.getNickname());
				}
				catch(Exception e) {
					server.sendToUser(user,e);
				}
			}
			/** Try to login if LoginUser was sent*/
			else if(receivedObj instanceof LoginUser) {
				System.out.println("Try Login Account");
				try {
					server.loginUser(user.getSocket(), accountOrLoginAsJson, user);
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println(user.getNickname()+" :"+receivedObj);
		}
		// end of Thread
		server.removeUser(user);
		//this.server.broadcastAllUsers();
		sc.close();
	}
}
