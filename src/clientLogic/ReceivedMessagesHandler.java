package clientLogic;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

class ReceivedMessagesHandler implements Runnable {

	private InputStream server;

	ReceivedMessagesHandler(InputStream server) {
		this.server = server;
	}

	// TODO differenzieren wenn Userlist vom Server kommt oder eine Nachricht
	// TODO Userlist schön anzeigen
	public void run() {
		Scanner s = new Scanner(server);
		String usernameAndMessage = "";
		while (s.hasNextLine()) {
			usernameAndMessage = s.nextLine();
			if (usernameAndMessage.charAt(0) == '[') {
				usernameAndMessage = usernameAndMessage.substring(1, usernameAndMessage.length() - 1);
				System.out.println("\nUSERS LIST: " + new ArrayList<>(Arrays.asList(usernameAndMessage.split(","))) + "\n");
			} else {
				try {
					System.out.println("\n" + usernameAndMessage);
				} catch (Exception ignore) {
				}
			}
		}
		s.close();
	}
}