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
import javafx.collections.ObservableSet;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import server.Server;

import java.sql.Timestamp;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static common.Constants.Contexts.*;

public class GUIController extends UIController {
	@FXML
	private Button btnSend, btnMyProfile, btnLogout;
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
	private ListView<Profile> tabContactsListView;
	@FXML
	private ImageView imgAvatar;
	@FXML
	private Label labelUserName, labelAbout;
	@FXML
	private VBox centerVbox;

	//Message broadcast
	private ObservableList<Message> broadcastObservableList;
	private ObservableList<Profile> profilesObservableList;
	private ObservableList<Profile> contactsObservableList;
	//FriendList

	//Group Contact (Left in Gui)
	private ObservableList<Group> groupsObservableList;
	private ObservableList<Profile> participantsObservableList;

	@FXML
	private void initialize() {
		/**Basic GUI initialize + Listener **/
		tabOnlineListView.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2)
				createNewChatTab(tabOnlineListView.getSelectionModel().getSelectedItem().getUserName());
		});
		tabContactsListView.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2)
				createNewChatTab(tabContactsListView.getSelectionModel().getSelectedItem().getUserName());
		});
		;
		btnSend.setOnMouseClicked(ev -> onSendClicked());
		textASendText.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER) onSendClicked();
		});
		/**get Instance of CLient **/
		client = Client.getInstance(Server.SERVER_HOST, Server.SERVER_PORT);
		/** Message Window initialize **/
		broadcastObservableList = FXCollections.observableArrayList();
		listViewBroadcast.setItems(broadcastObservableList);
		listViewBroadcast.setCellFactory(messagesListView -> new ChatListViewCell(listViewBroadcast.prefWidthProperty()));
		tabPaneChat.getTabs().get(0).setId(BROADCAST);
		/**display online Profiles initialize **/
		profilesObservableList = FXCollections.observableArrayList();
		tabOnlineListView.setItems(profilesObservableList);
		tabOnlineListView.setCellFactory(profilesListView -> new ContactListViewCell(tabOnlineListView.prefWidthProperty()));
		/**display contacts Profiles initialize**/
		contactsObservableList = FXCollections.observableArrayList();
		tabContactsListView.setItems(contactsObservableList);
		tabContactsListView.setCellFactory(profileListView -> new ContactListViewCell(tabContactsListView.prefWidthProperty()));
		/**groupchats **/
		tabGroupChatListView.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2)
				createNewChatTab(tabGroupChatListView.getSelectionModel().getSelectedItem().getName());
		});
		groupsObservableList = FXCollections.observableArrayList();
		tabGroupChatListView.setItems(groupsObservableList);
		tabGroupChatListView.setCellFactory(groupsListView -> new GroupChatListViewCell());
		//groupsObservableList.add(new Group("test", new Profile("a",0,null,null)));
		/**list participants of the selected group**/
		participantsObservableList = FXCollections.observableArrayList();
		listVInfo.setItems(participantsObservableList);
		listVInfo.setCellFactory(profilesListView -> new ContactListViewCell(listVInfo.prefWidthProperty()));
		/**Online List Context Menu**/
		ContextMenu contextMenuOnlineUsers = new ContextMenu();
		MenuItem addFriend = new MenuItem("Add");
		MenuItem showProfile = new MenuItem("show Profile");
		addFriend.setOnAction(e -> addFriend(tabOnlineListView.getSelectionModel().getSelectedItem()));
		contextMenuOnlineUsers.getItems().add(addFriend);
		contextMenuOnlineUsers.getItems().add(showProfile);
		tabOnlineListView.setContextMenu(contextMenuOnlineUsers);
		/**Group List Context Menu**/
		btnLogout.setOnMouseClicked(e -> onLogoutClicked());
		VBox.setVgrow(tabPaneChat, Priority.ALWAYS);
		loadAccount(client.getAccount());
		tabPaneChat.getSelectionModel().selectedItemProperty().addListener((observable, tabOld, tabNew) -> {
			if (tabNew.getId().startsWith("#")) {
				Group g = groupsObservableList.stream().filter(group -> tabNew.getId().equals(group.getName())).findFirst().get();
				participantsObservableList.clear();
				for (Profile p : g.getParticipants()) {
					participantsObservableList.add(p);
				}
			}else{
			    participantsObservableList.clear();
            }
		});
		//TODO (Next Sprint) use ListView(of ContactList).getSelectionModel().selectedItemProperty().bind() to show correct chat

	}

	private void onLogoutClicked() {
		client.sendToServer(LOGOFF, client.getAccount());
		client.setAccount(null);
		client.closeCurrentWindowNoexit();
		client.openWindow("login");
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

	private void addFriend(Profile selectedUser) {
		client.sendToServer(ADD_FRIEND, selectedUser);
	}

	private void onSendClicked() {
		if (!textASendText.getText().isEmpty()) {
			Profile selectedUser = tabOnlineListView.getSelectionModel().getSelectedItem();
			String receiver = tabPaneChat.getSelectionModel().getSelectedItem().getId();
			//String receiver = selectedUser==null? "broadcast" : selectedUser.getUserName();
			Message newMessage = new Message(checkTranslate.isSelected(), textASendText.getText(),
					new Timestamp(System.currentTimeMillis()), client.getAccount().getUserName(), receiver);
			//Display new message in chat
			ListView<Message> lv = (ListView<Message>) tabPaneChat.getSelectionModel().getSelectedItem().getContent();
			lv.getItems().add(newMessage);
			textASendText.clear();
			client.sendToServer(MESSAGE_SENT, newMessage);
			//client.sendObject(newMessage);
		} else {

		}
	}

	public void displayNewMessage(Message message) {
		Tab displayTab = null;
		if (message.getReceiver().equals(BROADCAST) || message.getReceiver().startsWith("#"))
			displayTab = createNewChatTab(message.getReceiver());
		else
			displayTab = createNewChatTab(message.getSender());
		//get ListView from tab to add text
		ListView<Message> lv = (ListView<Message>) displayTab.getContent();
		lv.getItems().add(message);
	}

	public void displayOnlineProfiles(ArrayList<Profile> profiles) {
		profilesObservableList.clear();
		for (Profile p : profiles) {
			profilesObservableList.add(p);
		}
	}

	public void displayContactsProfiles(ArrayList<Profile> profiles) {
		contactsObservableList.clear();
		for (Profile p : profiles) {
			contactsObservableList.add(p);
		}
	}

	public void displayGroupChats(ArrayList<Group> groups) {
		groupsObservableList.clear();
		for (Group g : groups)
			groupsObservableList.add(g);
	}

	public void onMyProfileClicked() {
		client.openWindow("UserProfile");
	}

	/**
	 * returns existing tab or create & returns new one
	 **/
	private Tab createNewChatTab(String tabName) {
		Tab newTab = new Tab();
		newTab.setText(tabName);
		//Check if tab already exists then return
		for (Tab tab : tabPaneChat.getTabs()) {
			if (tab.getId().equals(tabName))
				return tab;
		}
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
		Platform.runLater(() -> ((Stage) btnSend.getScene().getWindow()).close());
	}
}
