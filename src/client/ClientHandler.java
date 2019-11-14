package client;

import client.presentation.GUIController;
import common.data.Account;
import common.data.ContactInfo;
import common.data.Message;
import common.data.Packet;
import javafx.application.Platform;

import java.io.InputStream;
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
		scanner = new Scanner(inputStream);
	}
	//TODO Substitue Scanner with BufferedReader?
	// TODO differenzieren wenn Userlist vom Server kommt oder eine Nachricht
	// TODO Userlist schön anzeigen

	public void run() {


		while (running && scanner.hasNextLine()) {
			/*
			usernameAndMessage = s.nextLine();
			if (usernameAndMessage.charAt(0) == '[') {
				usernameAndMessage = usernameAndMessage.substring(1, usernameAndMessage.length() - 1);
				System.out.println("\nUSERS LIST: " + new ArrayList<>(Arrays.asList(usernameAndMessage.split(","))) + "\n");
			} else {
				try {
					System.out.println("\n" + usernameAndMessage);
				} catch (Exception ignore) {
				}
			}*/
			Packet receivedPacket=getPacketFromJson(scanner.nextLine());
			if(receivedPacket.getContext().contains(FAIL))
				System.err.println("from Server\t:\t"+receivedPacket);
			else
				System.out.println("from Server\t:\t"+receivedPacket);
			switch(receivedPacket.getContext()){
				case LOGIN_SUCCESS:
					Account account= (Account)receivedPacket.getData();
					//TODO check if not already logged in on other Connection
					client.setAccount(account);
					System.out.println("Logged into "+account);
					client.closeCurrentWindowNoexit();
					client.openWindow("gui");
                    /** Request online user list from server -> Receive: case USERLIST */
                    client.sendToServer(USERLIST,null);
					break;
				case MESSAGE_RECEIVED:
					Message msg= (Message) receivedPacket.getData();
					//if(msg.getReceiver()==null)
					    Platform.runLater(()->{client.getGUIController().displayNewMessage(msg);});
					break;
				case USERLIST:
					List<ContactInfo> userList= (List<ContactInfo>)receivedPacket.getData();
					System.out.println("Contacts received:");
					userList.forEach(u-> System.out.println(u));
					//TODO client.getGUIController().displayOnlineUsers(userList);
					break;
				default:
					break;
			}

		}
		scanner.close();
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