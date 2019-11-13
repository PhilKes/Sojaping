package client;

import common.data.Account;
import common.data.Packet;

import java.io.InputStream;
import java.util.Scanner;

import static common.JsonHelper.getPacketFromJson;
import static common.Constants.Contexts.*;

class ClientHandler implements Runnable {

	private InputStream server;
	private boolean running;

	ClientHandler(InputStream server) {
		this.server = server;
		this.running=true;
	}
	//TODO Substitue Scanner with BufferedReader?
	// TODO differenzieren wenn Userlist vom Server kommt oder eine Nachricht
	// TODO Userlist schön anzeigen

	public void run() {

		Scanner sc = new Scanner(server);
		while (running && sc.hasNextLine()) {
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
			String receivedJson=sc.nextLine();
			Packet receivedPacket=getPacketFromJson(receivedJson);
			if(receivedPacket.getContext().contains("Fail"))
				System.err.println("from Server\t:\t"+receivedPacket);
			else
				System.out.println("from Server\t:\t"+receivedPacket);
			switch(receivedPacket.getContext()){
				case LOGIN_SUCCESS:
					Account account= (Account)receivedPacket.getData();
					System.out.println("Logged into "+account);
					//TODO Login success -> show gui.fxml
					break;
				default:
					break;
			}

		}
		sc.close();
	}
}