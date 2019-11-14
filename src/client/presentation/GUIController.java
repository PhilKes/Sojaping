package client.presentation;


import client.Client;
import common.data.Message;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import server.Server;

import java.sql.Timestamp;

public class GUIController {
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
			//Todo send Message to Server, Sender / Receiver -> Message
			//client.sendObject(newMessage);
		}
		else{

		}
	}
	private void displayNewMessage(Message Message){
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

}
