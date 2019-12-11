package client.presentation.windows;

import client.Client;
import client.presentation.UIControllerWithInfo;
import client.presentation.listcells.ChatListViewCell;
import client.presentation.listcells.ContactListViewCell;
import client.presentation.listcells.GroupChatListViewCell;
import common.Util;
import common.data.Account;
import common.data.Group;
import common.data.Message;
import common.data.Profile;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import server.Server;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static common.Constants.Contexts.*;

public class GUIController extends UIControllerWithInfo {
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

	private Menu addToGroupChat, addToGroupChat1;

	@FXML
	private void initialize() {
		/**Basic GUI initialize + Listener **/
		addListener();
		/**get Instance of Client **/
		client = Client.getInstance(Server.SERVER_HOST, Server.SERVER_PORT);
		/** Message Window initialize **/
		initializeChatWindow();
		/** User Tab Pane(Left) initialize **/
		initializeTabPaneUsers();
		/**list participants of the selected group**/
		participantsObservableList = FXCollections.observableArrayList();
		listVInfo.setItems(participantsObservableList);
		listVInfo.setCellFactory(profilesListView -> new ContactListViewCell(listVInfo.prefWidthProperty()));
		/**Group List Context Menu**/
		btnLogout.setOnMouseClicked(e -> onLogoutClicked());
		labelAbout.setWrapText(true);
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
		/** Visual Notification**/
		this.initializeNotificationHandling();
		//TODO (Next Sprint) use ListView(of ContactList).getSelectionModel().selectedItemProperty().bind() to show correct chat

	}

	private void initializeNotificationHandling(){
		this.tabOnlineListView.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2)
				createNewChatTab(this.tabOnlineListView.getSelectionModel().getSelectedItem().getUserName());
		});

		this.textASendText.setOnMouseClicked(ev -> this.removeNotification());

		this.tabPaneChat.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
				Tab selectedTab = tabPaneChat.getTabs().get(newValue.intValue());
				if (selectedTab != null && selectedTab.getStyleClass().contains("tab-notification")) {
					selectedTab.getStyleClass().remove("tab-notification");
				}
			}
		});
	}

	private void removeNotification() {
		String selectedUserName = this.tabPaneChat.getSelectionModel().getSelectedItem().getId();
		Optional<Tab> selectedTabPane = this.tabPaneChat.getTabs().stream().filter(tab -> tab.getText().toLowerCase().equals(selectedUserName.toLowerCase())).findFirst();

		selectedTabPane.ifPresent(tab -> {
			if (tab.getStyleClass().contains("tab-notification")) {
				tab.getStyleClass().remove("tab-notification");
			}
		});
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
	public void loadAccount(Account acc) {
		labelUserName.setText(acc.getUserName());
		labelAbout.setText(acc.getAboutMe());
		//TODO Load Profilepicture (from DB?)
		/** Default Avatar */
		if(acc.getProfilePicture()==null) {
			imgAvatar.setImage(Util.getDefaultAvatar());
		}
	}

	private void displayGroupInfo() {

	}
	private void addFriend(Profile selectedUser) {
		client.sendToServer(ADD_FRIEND, selectedUser);
	}

	/**
	 * send text in text area to server as a message and displays the text in the currently active chat window
	 **/
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
			lv.scrollTo(lv.getItems().size() - 1);
			textASendText.clear();
			client.sendToServer(MESSAGE_SENT, newMessage);

		} else {

		}
	}

	/**
	 * for displaying received messages, one messages are displayed via onSendClicked method
	 **/
	public void displayNewMessage(Message message) {
		Tab displayTab = null;
		if (message.getReceiver().equals(BROADCAST) || message.getReceiver().startsWith("#"))
			displayTab = createNewChatTab(message.getReceiver());
		else
			displayTab = createNewChatTab(message.getSender());
		//get ListView from tab to add text
		ListView<Message> lv = (ListView<Message>) displayTab.getContent();
		lv.getItems().add(message);
		displayTab.getStyleClass().add("tab-notification");
		lv.scrollTo(lv.getItems().size() - 1);
	}

	public void displayOnlineProfiles(ArrayList<Profile> profiles) {
		profilesObservableList.clear();
		profilesObservableList.addAll(profiles);
		/** Update status of contacts*/
		for(int i=0; i<contactsObservableList.size(); i++) {
			Profile contact=contactsObservableList.get(i);
			Optional<Profile> profile=profiles.stream().filter(p -> p.getUserName().equals(contact.getUserName())).findFirst();
			if (profile.isPresent()) {
				contact.setStatus(profile.get().getStatus());
			} else {
				contact.setStatus(0);
			}
			contactsObservableList.set(i, contact);
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
		tabPaneChat.getSelectionModel().selectedItemProperty().addListener((observable, tabOld, tabNew) -> {
			if(tabNew.getId().startsWith("#")){
				Group g = groupsObservableList.stream().filter(group -> tabNew.getId().equals(group.getName())).findFirst().get();
				participantsObservableList.clear();
				for (Profile p : g.getParticipants()) {
					participantsObservableList.add(p);
				}
			} else {
				participantsObservableList.clear();
			}
		});
	}

	public void fillContextMenuAddToGroup(ArrayList<Group> groups) {
		//addToGroupChat.getItems().clear();
		addToGroupChat1.getItems().clear();
		for (Group g : groups) {
			MenuItem group = new MenuItem(g.getName());
			group.setOnAction(ev -> {
				System.out.println("Clicked on " + g.getName());
				System.out.println("On profile " + tabContactsListView.getSelectionModel().getSelectedItem());
				Group myGroup = groups.stream().filter(gr -> gr.getName().equals(group.getText())).findFirst().get();
				myGroup.addParticipant(tabContactsListView.getSelectionModel().getSelectedItem());
				//client.sendToServer(GROUP_UPDATE, myGroup);
			});
			//addToGroupChat.getItems().add(group);
			addToGroupChat1.getItems().add(group);
		}
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

	/**
	 * adds listener to gui
	 **/
	private void addListener() {
		btnSend.setOnMouseClicked(ev -> onSendClicked());
		textASendText.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER) onSendClicked();
		});
	}

	/**
	 * initializes Chat ListView with 1 tab for broadcast
	 **/
	private void initializeChatWindow() {
		broadcastObservableList = FXCollections.observableArrayList();
		listViewBroadcast.setItems(broadcastObservableList);
		listViewBroadcast.setCellFactory(messagesListView -> new ChatListViewCell(listViewBroadcast.prefWidthProperty()));
		tabPaneChat.getTabs().get(0).setId(BROADCAST);
	}

	private String createGUI() {
		Stage window = new Stage();
		window.initModality(Modality.APPLICATION_MODAL);
		window.setMinWidth(250);

		TextField textField = new TextField();

		Button yButton = new Button("Create");
		Button nbButton = new Button("Cancel");
		AtomicReference<String> exitCode = new AtomicReference<>();
		yButton.setOnAction(e -> {
			exitCode.set(textField.getText());
			window.close();
		});

		nbButton.setOnAction(e -> {
			exitCode.set(null);
			window.close();
		});


		VBox layout = new VBox(5);
		HBox buttons = new HBox(200);
		buttons.getChildren().addAll(yButton, nbButton);
		layout.getStylesheets().add(getClass().getResource("../resources/main.css").toExternalForm());
		layout.getChildren().addAll(textField, buttons);
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout);
		window.setScene(scene);
		window.showAndWait();
		return exitCode.get();
	}

	private void createNewGroup(String groupName, Profile firstMember) {
		System.out.println(groupName);
		groupName = "#" + groupName;
		Group newGroup = new Group(groupName, client.getAccount().getProfile());
		newGroup.addParticipant(firstMember);
		client.sendToServer(GROUP_UPDATE, newGroup);
	}

	private void initializeTabPaneUsers() {
		/**display online Profiles initialize **/
		profilesObservableList = FXCollections.observableArrayList();
		tabOnlineListView.setItems(profilesObservableList);
		tabOnlineListView.setCellFactory(profilesListView -> new ContactListViewCell(tabOnlineListView.prefWidthProperty()));
		/**display contacts Profiles initialize**/
		contactsObservableList = FXCollections.observableArrayList();
		tabContactsListView.setItems(contactsObservableList);
		tabContactsListView.setCellFactory(profileListView -> new ContactListViewCell(tabContactsListView.prefWidthProperty()));
		/**display groups initialize**/
		groupsObservableList = FXCollections.observableArrayList();
		tabGroupChatListView.setItems(groupsObservableList);
		tabGroupChatListView.setCellFactory(groupsListView -> new GroupChatListViewCell());
		/** add double click listener to tabs**/
		tabOnlineListView.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2)
				createNewChatTab(tabOnlineListView.getSelectionModel().getSelectedItem().getUserName());
		});
		tabContactsListView.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2)
				createNewChatTab(tabContactsListView.getSelectionModel().getSelectedItem().getUserName());
		});
		tabGroupChatListView.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2)
				createNewChatTab(tabGroupChatListView.getSelectionModel().getSelectedItem().getName());
		});
		/**Online List Context Menu**/
		//TODO Add to Group Chat, Show Profile, RemoveFriend
		ContextMenu contextMenuOnlineUsers = new ContextMenu();
		contextMenuOnlineUsers.setId("OnlineUsersContextMenu");
		MenuItem addFriend = new MenuItem("Add as Friend");
		MenuItem createChat = new MenuItem("Create Chat");
		MenuItem showProfile = new MenuItem("Show Profile");
		openPublicProfile(showProfile, tabOnlineListView.getSelectionModel());
		addFriend.setOnAction(e ->
				addFriend(tabOnlineListView.getSelectionModel().getSelectedItem()));
		createChat.setOnAction(e ->
				createNewChatTab(tabOnlineListView.getSelectionModel().getSelectedItem().getUserName()));
		contextMenuOnlineUsers.getItems().addAll(addFriend, createChat, addToGroupChat, showProfile);
		tabOnlineListView.setContextMenu(contextMenuOnlineUsers);
		/**Contact List Context Menu**/
		ContextMenu contextMenuContacts = new ContextMenu();
		contextMenuContacts.setId("ContactsContextMenu");
		MenuItem createChat1 = new MenuItem("Create Chat");
		Menu addToGroupChat = new Menu("Add to Group Chat");
		MenuItem showProfile1 = new MenuItem("Show Profile");
		MenuItem createNewGroup = new MenuItem("Create New Group");
		openPublicProfile(showProfile1, tabContactsListView.getSelectionModel());
		MenuItem removeFriend = new MenuItem("Remove Friend");
		addToGroupChat.getItems().add(createNewGroup);
		createChat1.setOnAction(e ->
				createNewChatTab(tabContactsListView.getSelectionModel().getSelectedItem().getUserName()));
		createNewGroup.setOnAction(e ->
				createNewGroup(createGUI(), tabContactsListView.getSelectionModel().getSelectedItem()));

		contextMenuContacts.getItems().addAll(createChat1, addToGroupChat, showProfile1, removeFriend);
		tabContactsListView.setContextMenu(contextMenuContacts);
	}
	private void openPublicProfile(final MenuItem showProfile1, final MultipleSelectionModel<Profile> selectionModel) {
		showProfile1.setOnAction(e ->
				{client.openWindow("PublicProfile");
					Platform.runLater(() -> {
						PublicProfileController publicProfileController = (PublicProfileController) client.getController();
						publicProfileController.setProfile(selectionModel.getSelectedItem());
					});
				}
		);
	}

	@Override
	public void close() {
		Platform.runLater(() -> ((Stage) btnSend.getScene().getWindow()).close());
	}
}
