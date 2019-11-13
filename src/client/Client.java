package client;

import common.data.Account;
import common.data.Packet;
import server.Connection;
import server.Server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

import static common.Constants.Contexts.*;

public class Client {

	private static Client instance;
	private LoginUser loginUser;

	private Account account;

	private String host;
	private Connection client;
	private int port;
	private PrintStream output;

	public static Client getInstance(String host, int port) {
		if(instance ==null)
			instance =new Client(host,port);
		return instance;
	}
	private Client(String host,int port){
		this.host = host;
		this.port = port;
		//this.loginUser = new LoginUser();
	}

	public static void main(String[] args) throws IOException {
		getInstance(Server.SERVER_HOST, Server.SERVER_PORT).run();
	}

	public void run()  {
		/** Loop to try to connect to server*/
		do {
			try {
				client=new Connection(host, port, host);
				break;
			}
			catch(IOException e) {
				//e.printStackTrace();
				System.err.println("Connection failed\n retrying in 3 Seconds...");
				try {
					Thread.sleep(3000);
				}
				catch(InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}while(true);
		System.out.println("Socket successfully established to server("+host+")!");
		output = client.getOutStream();
		sendToServer(CONNECT,client.getSocket().getInetAddress().getHostAddress());
		/** Start Thread to handle packets from the server*/
		new Thread(new ClientHandler(client.getInputStream())).start();
	}
	public void stop(){
		output.close();
		try {
			client.getSocket().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Send Object as JSON to Server*/
	public void sendToServer(String context,Object object){
		Packet packet=new Packet(context,object);
		output.println(packet.getJson());
		System.out.println("to Server\t:\t"+packet);
	}
}

