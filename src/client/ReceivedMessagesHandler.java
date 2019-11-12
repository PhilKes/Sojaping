package client;

import java.io.InputStream;
import java.util.Scanner;

import static common.JsonHelper.convertJsonToObject;


class ReceivedMessagesHandler implements Runnable {

	private InputStream server;
	private boolean running;

	ReceivedMessagesHandler(InputStream server) {
		this.server = server;
		this.running=true;
	}

	// TODO differenzieren wenn Userlist vom Server kommt oder eine Nachricht
	// TODO Userlist schön anzeigen
	public void run() {
		//TODO Substitue Scanner with BufferedReader?
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
			//TODO receive proper responses from server
			String jsonReceived=sc.nextLine();
			if(jsonReceived.isEmpty()){
				System.err.println("Received empty message from Server");
			}else{
				System.out.println("SERVER: "+convertJsonToObject(jsonReceived));
			}
		}
		sc.close();
	}
}