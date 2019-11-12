package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

public class User {

	private PrintStream streamOut;

	private InputStream streamIn;

	private String nickname;

	private Socket socket;

	public User(Socket client, String name) throws IOException {
		this.socket=client;
		this.streamOut = new PrintStream(client.getOutputStream());
		this.streamIn = client.getInputStream();
		this.nickname = name;
	}

	public PrintStream getOutStream() {
		return this.streamOut;
	}

	public InputStream getInputStream() {
		return this.streamIn;
	}

	public Socket getSocket() {
		return socket;
	}

	public String getNickname() {
		return this.nickname;
	}
}
