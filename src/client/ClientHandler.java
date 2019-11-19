package client;

import common.data.Account;
import common.data.Profile;
import common.data.Message;
import common.data.Packet;
import javafx.application.Platform;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static common.JsonHelper.getPacketFromJson;
import static common.Constants.Contexts.*;

class ClientHandler implements Runnable {

	private Client client;
	private boolean running;
	private Scanner scanner;

	public ClientHandler(Client client, InputStream inputStream ) {
		this.client=client;
		this.running=true;
		scanner = new Scanner(inputStream,"UTF-8");
	}
	// TODO differenzieren wenn Userlist vom Server kommt oder eine Nachricht
	// TODO Userlist schön anzeigen

	public void run() {
		while (running && scanner.hasNextLine()) {
			Packet receivedPacket=getPacketFromJson(scanner.nextLine());
			try {
				handlePacket(receivedPacket);
			}
			catch(Exception e) {
				System.err.println("Invalid JSON received");
				client.sendToServer(FAIL,e);
			}
		}
		scanner.close();
	}

	private void handlePacket(Packet receivedPacket) throws Exception {
		if(receivedPacket==null)
			throw new Exception("Invalid JSON received");
		if(receivedPacket.getContext().contains(FAIL))
			System.err.println("from Server\t:\t"+receivedPacket);
		else
			System.out.println("from Server\t:\t"+receivedPacket);
		switch(receivedPacket.getContext()){
			case LOGIN_SUCCESS:
				Account account= receivedPacket.getData();
				//TODO (Next Sprint) check if not already logged in on other Connection
				client.setAccount(account);
				System.out.println("Logged into "+account);
				client.closeCurrentWindowNoexit();
				client.openWindow("gui");
				///** Request online user list from server -> Receive: case USERLIST */
				//client.sendToServer(USERLIST,null);
				break;
			case MESSAGE_RECEIVED:
				Message msg= receivedPacket.getData();
				//if(msg.getReceiver()==null)
				Platform.runLater(()->{client.getGUIController().displayNewMessage(msg);});
				break;
			case USERLIST:
				ArrayList<Profile> userList=receivedPacket.getData();
				System.out.println("Profiles received:");
				userList.forEach(u-> System.out.println(u));
				Platform.runLater(()->{client.getGUIController().displayOnlineProfiles(userList);});
				//TODO --> client.getGUIController().displayOnlineUsers(userList);
				break;
			default:
				break;
		}
	}

	public boolean waitForConnectSuccess(){
		Packet response= getPacketFromJson(scanner.nextLine());
		if(response.getContext().equals(CONNECT_SUCCESS)){
			System.out.println("CONNECT_SUCCESS Packet received");
			return true;
		}
		System.err.println("CONNECT_SUCCESS Packet not received!");
		return false;
	}
}