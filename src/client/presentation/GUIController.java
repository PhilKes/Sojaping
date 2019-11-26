package client.presentation;


import client.Client;
import common.data.Group;
import common.data.Message;
import common.data.Profile;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import server.Server;

import java.sql.Timestamp;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
	private CheckBox checkTranslate;
    @FXML
    private CheckBox checkBroadcast;
    @FXML
    private TabPane tabPaneChat;
	@FXML
	private ListView<Message> listViewBroadcast;
	@FXML
	private ListView<Profile> tabOnlineListView;
	@FXML
	private ListView<Group> tabGroupChatListView;
	@FXML
	private ListView<Profile> listVInfo;

	//Message broadcast
	private ObservableList<Message> broadcastObservableList;
	private ObservableList<Profile> profilesObservableList;
	//Group Contact (Left in Gui)
	private ObservableList<Group> groupsObservableList;
	private ObservableList<Profile> participantsObservableList;

	//Todo ListView does not Auto scroll to newest message

	@FXML
	private void initialize() {
		//Basic GUI initialize + Listener
		tabOnlineListView.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2)
				createNewChatTab(tabOnlineListView.getSelectionModel().getSelectedItem().getUserName());
		});
		btnSend.setOnMouseClicked(ev -> onSendClicked());
		textASendText.setOnKeyReleased(event -> {if(event.getCode() == KeyCode.ENTER)onSendClicked();});
		client=Client.getInstance(Server.SERVER_HOST, Server.SERVER_PORT);
		// Message Window initialize
		broadcastObservableList = FXCollections.observableArrayList();
		listViewBroadcast.setItems(broadcastObservableList);
		listViewBroadcast.setCellFactory(messagesListView -> new ChatListViewCell());
		tabPaneChat.getTabs().get(0).setId("broadcast");
		//display online Profiles initialize
		profilesObservableList = FXCollections.observableArrayList();
		tabOnlineListView.setItems(profilesObservableList);
		tabOnlineListView.setCellFactory(profilesListView -> new ContactListViewCell());
		//groupchats
		tabGroupChatListView.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2)
				createNewChatTab(tabGroupChatListView.getSelectionModel().getSelectedItem().getName());
		});
		groupsObservableList = FXCollections.observableArrayList();
		tabGroupChatListView.setItems(groupsObservableList);
		tabGroupChatListView.setCellFactory(groupsListView -> new GroupChatListViewCell());
		groupsObservableList.add(new Group("test", new Profile("a",0,null,null)));
		//list participants of the selected group
		participantsObservableList = FXCollections.observableArrayList();
		listVInfo.setItems(participantsObservableList);
		listVInfo.setCellFactory(profilesListView -> new ContactListViewCell());

		//TODO (Next Sprint) use ListView(of ContactList).getSelectionModel().selectedItemProperty().bind() to show correct chat

	}

	private void displayGroupInfo() {
		//TODO: how to get the info, that this is a group?
		//tabPaneChat.getSelectionModel().getSelectedItem().getText();
		// gibt Name von Tab zurück
	}


	private void onSendClicked() {
		if(!textASendText.getText().isEmpty()){
			Profile selectedUser = tabOnlineListView.getSelectionModel().getSelectedItem();
			String receiver = tabPaneChat.getSelectionModel().getSelectedItem().getId();
			//String receiver = selectedUser==null? "broadcast" : selectedUser.getUserName();
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

	//ToDo Sender -> show Tab with Receiver || Receiver -> show Tab with sender
	public void displayNewMessage(Message message) {
		// createNewChatTab returns existing tab or returns new one
		Tab displayTab = createNewChatTab(message.getReceiver());
		//add text to right tab
		ListView<Message> lv = (ListView<Message>) displayTab.getContent();
		lv.getItems().add(message);
	}

	public void displayOnlineProfiles(ArrayList<Profile> profiles) {
        profilesObservableList.clear();
        for (Profile p : profiles) {
            profilesObservableList.add(p);
        }
    }
	private void onMyProfileClicked(){

	}

	private Tab createNewChatTab(String tabName) {
		Tab newTab = new Tab();
		newTab.setText(tabName);
		//Check if tab already exists then return
		for (Tab tab : tabPaneChat.getTabs()) {
			if (tab.getId().equals(tabName))
				return tab;
		}
		//max of Tabs 10
		ObservableList<Message> newObservableList = FXCollections.observableArrayList();
		ListView<Message> newListView = new ListView<>();
		newListView.setItems(newObservableList);
		newListView.setCellFactory(messagesListView -> new ChatListViewCell());
		newTab.setContent(newListView);
		newTab.setId(tabName);
		tabPaneChat.getTabs().add(newTab);
		return newTab;
	}
	@Override
	public void close() {
		Platform.runLater(()-> ((Stage)btnSend.getScene().getWindow()).close());
	}
}
