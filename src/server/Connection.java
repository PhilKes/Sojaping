package server;

import common.data.Account;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

/** Socket connection to host with Input/Output Streams and loggedAcount if logged in*/
public class Connection {

	private PrintStream streamOut;
	private InputStream streamIn;
	private String nickname;
	private Socket socket;

	private Account loggedAccount;

	public Connection(String host,int port, String name) throws IOException {
		this.socket=new Socket(host,port);
		this.streamOut = new PrintStream(socket.getOutputStream());
		this.streamIn = socket.getInputStream();
		this.nickname = name;
		this.loggedAccount=null;
	}

	public Connection(Socket socket, String name) throws IOException{
		this.socket	=	socket;
		this.streamOut = new PrintStream(socket.getOutputStream());
		this.streamIn = socket.getInputStream();
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

	public Account getLoggedAccount() {
		return loggedAccount;
	}

	public void setLoggedAccount(Account loggedAccount) {
		this.loggedAccount=loggedAccount;
	}
	public boolean isLoggedIn(){
		return this.loggedAccount!=null;
	}
}
