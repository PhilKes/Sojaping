package client.presentation;

import client.Client;
import common.Util;
import common.data.Account;
import common.data.Group;
import common.data.Message;
import common.data.Profile;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import server.Server;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Optional;

import static common.Constants.Contexts.BROADCAST;
import static common.Constants.Contexts.MESSAGE_SENT;

public class GUIController extends UIController {
	@FXML
	private Button btnSend, btnMyProfile;
	@FXML
	private TextArea textASendText;
	@FXML
	private CheckBox checkTranslate;
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
	@FXML
	private ImageView imgAvatar;
	@FXML
	private Label labelUserName, labelAbout;

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
		textASendText.setOnMouseClicked(ev -> this.removeNotification());
		client=Client.getInstance(Server.SERVER_HOST, Server.SERVER_PORT);

		// Message Window initialize
		broadcastObservableList = FXCollections.observableArrayList();
		listViewBroadcast.setItems(broadcastObservableList);
		listViewBroadcast.setCellFactory(messagesListView -> new ChatListViewCell(listViewBroadcast.prefWidthProperty()));
		tabPaneChat.getTabs().get(0).setId(BROADCAST);
		//display online Profiles initialize
		profilesObservableList = FXCollections.observableArrayList();
		tabOnlineListView.setItems(profilesObservableList);
		tabOnlineListView.setCellFactory(profilesListView -> new ContactListViewCell(tabOnlineListView.prefWidthProperty()));
		//groupchats
		tabGroupChatListView.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2)
				createNewChatTab(tabGroupChatListView.getSelectionModel().getSelectedItem().getName());
		});
		groupsObservableList = FXCollections.observableArrayList();
		tabGroupChatListView.setItems(groupsObservableList);
		tabGroupChatListView.setCellFactory(groupsListView -> new GroupChatListViewCell());
		//groupsObservableList.add(new Group("test", new Profile("a",0,null,null)));
		//list participants of the selected group
		participantsObservableList = FXCollections.observableArrayList();
		listVInfo.setItems(participantsObservableList);
		listVInfo.setCellFactory(profilesListView -> new ContactListViewCell(listVInfo.prefWidthProperty()));

		loadAccount(client.getAccount());

	}
	private void removeNotification() {
		String selectedUserName = this.tabOnlineListView.getSelectionModel().getSelectedItem().getUserName();
		Optional<Tab> selectedTabPane = this.tabPaneChat.getTabs().stream().filter(tab -> tab.getText().equals(selectedUserName)).findFirst();
		selectedTabPane.ifPresent(tab -> tab.getStyleClass().remove("tab-notification"));
	}

	/**
	 * Load Account into My Profile (Top left)
	 */
	private void loadAccount(Account acc) {
		labelUserName.setText(acc.getUserName());
		labelAbout.setText(acc.getAboutMe());
		//TODO Load Profilepicture (from DB?)
		/** Default Avatar */
		if(acc.getProfilePicture()==null) {
			imgAvatar.setImage(Util.getDefaultAvatar());
		}
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
			//Display new message in chat
			ListView<Message> lv = (ListView<Message>) tabPaneChat.getSelectionModel().getSelectedItem().getContent();
			lv.getItems().add(newMessage);
			textASendText.clear();
			client.sendToServer(MESSAGE_SENT,newMessage);
			//client.sendObject(newMessage);
		}
		else{

		}
	}

	//ToDo Sender -> show Tab with Receiver || Receiver -> show Tab with sender
	public void displayNewMessage(Message message) {
        Tab displayTab = null;
        if (message.getReceiver().equals(BROADCAST))
            displayTab = createNewChatTab(message.getReceiver());
        else
            displayTab = createNewChatTab(message.getSender());

		ListView<Message> lv = (ListView<Message>) displayTab.getContent();

		displayTab.getStyleClass().add("tab-notification");
		lv.getItems().add(message);
	}

	public void displayOnlineProfiles(ArrayList<Profile> profiles) {
        profilesObservableList.clear();
        for (Profile p : profiles) {
            profilesObservableList.add(p);
        }
    }

	public void onMyProfileClicked(){
		client.openWindow("UserProfile");
	}

	//returns existing tab or create & returns new one
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
		newListView.setCellFactory(messagesListView -> new ChatListViewCell(newListView.prefWidthProperty()));
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
