package server;

import common.data.LoginUser;
import common.data.Account;
import common.data.Message;
import common.data.Packet;

import static common.Constants.Contexts.*;
import static common.JsonHelper.getPacketFromJson;

public class ServerHandler implements Runnable {

	private Server server;
	private Connection connection;
	private boolean running;

	private String receivedJson;

	public ServerHandler(Server server, Connection connection) {
		this.server = server;
		this.connection=connection;
		this.running=true;
		//this.server.broadcastAllUsers();
	}

	public ServerHandler(Server server, Connection connection, String json) {
		this.receivedJson=json;
		this.server = server;
		this.connection=connection;
		this.running=true;
	}

	/** Handles single Packet received from connection InputStream*/
	public void run() {
		Packet receivedPacket=getPacketFromJson(receivedJson);
		if(receivedPacket==null) {
			server.sendToUser(connection, FAIL, "Invalid JSON received!");
			return;
		}
		if(receivedPacket.getContext().contains(FAIL))
			System.err.println("from "+connection.getNickname()+"\t:\t"+receivedPacket);
		else
			System.out.println("from "+connection.getNickname()+"\t:\t"+receivedPacket);
		try {
			handlePacket(receivedPacket);
		}catch(Exception e){
			server.sendToUser(connection, receivedPacket.getContext()+FAIL, e);
		}
		//server.removeUser(connection);
		//this.server.broadcastAllUsers();
	}

	//TODO Distinguish Exceptions -> do not throw general Exception()
	private void handlePacket(Packet receivedPacket) throws Exception {
		/** Determine context of receivedPacket*/
		switch(receivedPacket.getContext()) {
			case REGISTER:
				/** Try to register account to DB, send Account from DB to user or send failed Exception */
				Account account= receivedPacket.getData();
				server.registerUser(connection.getSocket(), account);
				server.sendToUser(connection, REGISTER_SUCCESS, "Hi new registered user " + connection.getNickname());
				break;
			/** Try to authenticate sent LoginUser*/
			case LOGIN:
				System.out.println("Login Account");
				LoginUser loginUser= receivedPacket.getData();
				/** Check login credentials, send Account from DB to user or throw failed Exception */
				Account loginAccount=server.loginUser(connection, loginUser);
				server.sendToUser(connection, LOGIN_SUCCESS, loginAccount);
				server.setLoggedUser(connection,loginAccount);
				//server.sendToUser(connection,INFO,"Hi welcome back  " + connection.getNickname());
				break;
			//TODO LOGOUT
			case MESSAGE_SENT:
				System.out.println("Send message");
				Message message= receivedPacket.getData();
				/** Check login credentials, send Account from DB to user or send failed Exception */
				String receiver= message.getReceiver();
				if(receiver==null){
					server.broadcastMessages(connection,message);
				}
				else{
					/** Private message */
					if(!server.sendMessage(message))
						throw new Exception("Receiver not found!");
				}
				break;
			case USERLIST:
				System.out.println("Send online User list");
                /** Send online Userlist to client*/
                //TODO Exclude the receiving client/User?
                server.sendToUser(connection,USERLIST,server.getOnlineUsers());
                break;
            default:
				System.err.println("Received unknown Packet context:\t"+receivedPacket.getContext());
				throw new Exception("Unknown Packet context('"+receivedPacket.getContext()+"') sent!");
		}
	}
}
