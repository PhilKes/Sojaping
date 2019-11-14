package client;

import client.presentation.GUIController;
import client.presentation.UIController;
import common.data.Account;
import common.data.Packet;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import server.Connection;
import server.Server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.Stack;

import static common.Constants.Contexts.*;

public class Client {

	private static Client instance;
	private LoginUser loginUser;

	private Account account;

	private String host;
	private Connection connection;
	private int port;
	private PrintStream output;

	private Stack<UIController> controllerStack;

	public static Client getInstance(String host, int port) {
		if(instance ==null)
			instance =new Client(host,port);
		return instance;
	}
	private Client(String host,int port){
		this.host = host;
		this.port = port;
		this.controllerStack=new Stack<>();
		//this.loginUser = new LoginUser();
	}

	public UIController getController() {
		return controllerStack.peek();
	}

	public void setController(UIController controller) {
		controllerStack.push(controller);
	}

	public static void main(String[] args) throws IOException {
		getInstance(Server.SERVER_HOST, Server.SERVER_PORT).run();
	}

	public void run()  {
		/** Loop to try to connect to server*/
		do {
			try {
				connection=new Connection(host, port, InetAddress.getLocalHost().getHostAddress());
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
		output = connection.getOutStream();
		//TODO Wait for CONNECT_SUCCESS Packet before starting ClientHandler
		ClientHandler handler=new ClientHandler(this, connection.getInputStream());
		sendToServer(CONNECT, connection.getSocket().getInetAddress().getHostAddress());
		if(handler.waitForConnectSuccess())
			/** Start Thread to handle packets from the server*/
			new Thread(handler).start();
	}
	public void stop(){
		output.close();
		try {
			connection.getSocket().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Send Object as JSON to Server*/
	//TODO MAKE ALL SENDTOSERVER ASYNCHRONOUS TASKS
	public void sendToServer(String context,Object object){
		Packet packet=new Packet(context,object);
		String json=packet.getJson();
		output.println(json);
		System.out.println("to Server\t:\t"+packet);
	}

	public void closeCurrentWindow() {
		closeCurrentWindowNoexit();
		if(controllerStack.isEmpty())
			this.stop();
	}
	public void closeCurrentWindowNoexit() {
		controllerStack.pop().close();
	}
	public void openWindow(String window) {
		Platform.runLater(()->{
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("presentation/"+window+".fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			//RegisterController registerCtrl=(RegisterController)fxmlLoader.getController();
			//registerCtrl.setClient(this);
			UIController controller= (UIController) fxmlLoader.getController();
			controller.setClient(this);
			setController(controller);
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.initStyle(StageStyle.DECORATED);
			stage.setTitle(window);
			stage.setScene(new Scene(root1));
			stage.setOnCloseRequest(ev->
					closeCurrentWindow());
			stage.show();
		}catch (Exception e){
			e.printStackTrace();
		}});
	}

	public void setAccount(Account account) {
		this.account=account;
	}

	public Account getAccount() {
		return account;
	}

	public GUIController getGUIController() {
		for(UIController controller : controllerStack){
			if(controller instanceof GUIController)
				return (GUIController)controller;
		}
		return null;
	}
}

