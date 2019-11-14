package server;

import client.LoginUser;
import common.data.Account;
import common.data.Message;
import common.data.Packet;

import java.util.Scanner;

import static common.Constants.Contexts.*;
import static common.JsonHelper.getPacketFromJson;

public class ServerHandler implements Runnable {

	private Server server;

	private Connection connection;
	private boolean running;

	public ServerHandler(Server server, Connection connection) {
		this.server = server;
		this.connection=connection;
		this.running=true;
		//this.server.broadcastAllUsers();
	}

	/** Handles all incoming messages from connection InputStream*/
	public void run() {
		Scanner sc = new Scanner(this.connection.getInputStream());
		/*while (sc.hasNextLine()) {
			message = sc.nextLine();
			server.broadcastMessages(message, user);
		}*/
		while(running && sc.hasNextLine()) {
			String receivedJson=sc.nextLine();
			Packet receivedPacket=getPacketFromJson(receivedJson);

			if(receivedPacket.getContext().contains("Fail"))
				System.err.println("from "+connection.getNickname()+"\t:\t"+receivedPacket);
			else
				System.out.println("from "+connection.getNickname()+"\t:\t"+receivedPacket);

			/** Determine method of receivecPacket*/
			switch(receivedPacket.getContext()) {
				case REGISTER:
					/** Try to register account to DB, send Account from DB to user or send failed Exception */
					try {
						Account account=(Account) receivedPacket.getData();
						server.registerUser(connection.getSocket(), account);
						server.sendToUser(connection, REGISTER_SUCCESS, "Hi new registered user " + connection.getNickname());
					}
					catch(Exception e) {
						server.sendToUser(connection, REGISTER_FAIL, e);
					}
					break;
				/** Try to login*/
				case LOGIN:
					System.out.println("Try Login Account");
					LoginUser loginUser=(LoginUser) receivedPacket.getData();
					/** Check login credentials, send Account from DB to user or send failed Exception */
					try {
						Account account=server.loginUser(connection, loginUser);
						server.sendToUser(connection, LOGIN_SUCCESS, account);
						//TODO Send Userlist, new messages to user, ...
						//server.sendToUser(connection,INFO,"Hi welcome back  " + connection.getNickname());
					}
					catch(Exception e) {
						server.sendToUser(connection, LOGIN_FAIL, e);
						//e.printStackTrace();
					}
					break;
				case MESSAGE_SENT:
					System.out.println("Try send message");
					Message message=(Message) receivedPacket.getData();
					/** Check login credentials, send Account from DB to user or send failed Exception */
					try {
						Account receiver= message.getReceiver();
						if(receiver==null){
							//TODO broadcast
							server.broadcastMessages(connection,message);
						}
						else{
							//TODO Private message
							//server.sendToUser(connection, MESSAGE_RECEIVED, message);
						}

						//TODO Send Userlist, new messages to user, ...
						//server.sendToUser(connection,INFO,"Hi welcome back  " + connection.getNickname());
					}
					catch(Exception e) {
						server.sendToUser(connection, MESSAGE_FAIL, e);
						//e.printStackTrace();
					}
					break;
				default:
					System.err.println("Received unknown Packet context:\t"+receivedPacket.getContext());
					break;
			}
		}
		// end of Thread
		server.removeUser(connection);
		//this.server.broadcastAllUsers();
		sc.close();
	}
}
