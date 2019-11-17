package client.presentation;


import client.Client;
import common.data.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import static common.Constants.Contexts.USERLIST;

public class GUIController extends UIController {
	@FXML
	private Button btnSend;
	@FXML
	private Button btnMyProfile;
	@FXML
	private TextArea textASendText;
	@FXML
	private ListView<Message> listVChat;
	@FXML
	private CheckBox checkTranslate;

	private ObservableList<Message> messageObservableList;

	//Todo Display all online users(status)
	//Todo Javafx custom array adapter for messages

	@FXML
	private void initialize() {
		btnSend.setOnMouseClicked(ev -> onSendClicked());
		textASendText.setOnKeyReleased(event -> {if(event.getCode() == KeyCode.ENTER)onSendClicked();});
		client=Client.getInstance(Server.SERVER_HOST, Server.SERVER_PORT);

		messageObservableList = FXCollections.observableArrayList();
		listVChat.setItems(messageObservableList);
		listVChat.setCellFactory(messagesListView -> new ChatListViewCell());

		//TODO (Next Sprint) use ListView(of ContactList).getSelectionModel().selectedItemProperty().bind() to show correct chat

	}

	private void onSendClicked() {
		if(!textASendText.getText().isEmpty()){
			//Todo Receiver Null = Broadcast otherwise fill receiver with information from GUI Contact list
			Message newMessage = new Message(checkTranslate.isSelected(), textASendText.getText(), new Timestamp(System.currentTimeMillis()),client.getAccount().getUserName(),null);
			displayNewMessage(newMessage);
			textASendText.clear();
			client.sendToServer(MESSAGE_SENT,newMessage);
			//Todo send Message to Server, Sender / Receiver -> Message
			//client.sendObject(newMessage);
		}
		else{

		}
	}
	public void displayNewMessage(Message message) {
		//listVChat = listVChat.getItems().add(listVChat.getItems().size(), )
		messageObservableList.add(message);
		//listVChat.getItems().add(listVChat.getItems().size(),message.getTimestamp().toString().split("\\.")[0] +" "+message.getSender()+": "+ message.getText());
	}

	private void onMyProfileClicked(){

	}
	private void onContactsClicked(){

	}
	private void onChatsClicked(){
		//necessary? Chats are not saved
	}

	@Override
	public void close() {
		Platform.runLater(()-> ((Stage)btnSend.getScene().getWindow()).close());
	}
}
