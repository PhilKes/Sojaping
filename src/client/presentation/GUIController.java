package client.presentation;


import client.Client;
import common.data.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import server.Server;

import java.sql.Timestamp;

import static common.Constants.Contexts.MESSAGE_SENT;

public class GUIController extends UIController {
	@FXML
	private Button btnSend;
	@FXML
	private Button btnMyProfile;
	@FXML
	private TextArea textASendText;
	@FXML
	private ListView listVChat;
	@FXML
	private CheckBox checkTranslate;

	private Client client;


	@FXML
	private void initialize() {
		btnSend.setOnMouseClicked(ev -> onSendClicked());
		textASendText.setOnKeyReleased(event -> {if(event.getCode() == KeyCode.ENTER)onSendClicked();});
		client=Client.getInstance(Server.SERVER_HOST, Server.SERVER_PORT);

	}

	private void onSendClicked() {
		if(!textASendText.getText().isEmpty()){
			Message newMessage = new Message(checkTranslate.isSelected(), textASendText.getText(), new Timestamp(System.currentTimeMillis()),client.getAccount(),null);
			displayNewMessage(newMessage);
			textASendText.clear();
			client.sendToServer(MESSAGE_SENT,newMessage);
			//Todo send Message to Server, Sender / Receiver -> Message
			//client.sendObject(newMessage);
		}
		else{

		}
	}
	public void displayNewMessage(Message Message){
		//Todo add Sender name to output String
		listVChat.getItems().add(listVChat.getItems().size(),Message.getTimestamp().toString().split("\\.")[0] +" "+Message.getSender().getUserName()+": "+ Message.getText());
	}

	private void onMyProfileClicked(){

	}
	private void onContactsClicked(){

	}
	private void onChatsClicked(){
		//necessary? Chats are not saved
	}


	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	@Override
	public void close() {
		Platform.runLater(()-> ((Stage)btnSend.getScene().getWindow()).close());
	}
}
