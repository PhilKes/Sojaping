package client.presentation;


import client.Client;
import common.data.Message;
import common.data.Profile;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import server.Server;

import java.sql.Timestamp;
import java.text.BreakIterator;
import java.util.ArrayList;

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
	@FXML
	private ListView<Profile> tabOnlineListView;

	private ObservableList<Message> messageObservableList;
	private ObservableList<Profile> profilesObservableList;

	//Todo ListView does not Auto scroll to newest message

	@FXML
	private void initialize() {
	    //Basic GUI initialize
		btnSend.setOnMouseClicked(ev -> onSendClicked());
		textASendText.setOnKeyReleased(event -> {if(event.getCode() == KeyCode.ENTER)onSendClicked();});
		client=Client.getInstance(Server.SERVER_HOST, Server.SERVER_PORT);
		// Message Window initialize
		messageObservableList = FXCollections.observableArrayList();
		listVChat.setItems(messageObservableList);
		listVChat.setCellFactory(messagesListView -> new ChatListViewCell());
		//display online Profiles initialize
		profilesObservableList = FXCollections.observableArrayList();
		tabOnlineListView.setItems(profilesObservableList);
		tabOnlineListView.setCellFactory(profilesListView -> new ContactListViewCell());

		//TODO (Next Sprint) use ListView(of ContactList).getSelectionModel().selectedItemProperty().bind() to show correct chat

	}

	private void onSendClicked() {
		if(!textASendText.getText().isEmpty()){
			Profile selectedUser = tabOnlineListView.getSelectionModel().getSelectedItem();
			String receiver = selectedUser==null? null : selectedUser.getUserName();

			Message newMessage = new Message(checkTranslate.isSelected(), textASendText.getText(),
					new Timestamp(System.currentTimeMillis()),client.getAccount().getUserName(), receiver);
			displayNewMessage(newMessage);
			textASendText.clear();
			client.sendToServer(MESSAGE_SENT,newMessage);

			//client.sendObject(newMessage);
		}
		else{

		}
	}
	public void displayNewMessage(Message message) {
		messageObservableList.add(message);
	}
	public void displayOnlineProfiles(ArrayList<Profile> profiles){
		profilesObservableList.clear();
		for (Profile p:profiles) {
			profilesObservableList.add(p);
		}
	}
	private void onMyProfileClicked(){

	}
	private void onContactsClicked(){

	}
	private void onChatsClicked(){
	}

	@Override
	public void close() {
		Platform.runLater(()-> ((Stage)btnSend.getScene().getWindow()).close());
	}
}
