package client.presentation.windows;

import client.Client;
import client.presentation.FXUtil;
import client.presentation.TitleBarController;
import client.presentation.UIControllerWithInfo;
import client.presentation.listcells.ChatListViewCell;
import client.presentation.listcells.ContactListViewCell;
import client.presentation.listcells.GroupChatListViewCell;
import common.Constants;
import common.data.Account;
import common.data.Group;
import common.data.Message;
import common.data.Profile;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static common.Constants.Contexts.*;
import static common.Constants.SERVER_HOST;
import static common.Constants.SERVER_PORT;

public class GUIController extends UIControllerWithInfo {
    @FXML
    private BorderPane rootPane;
    @FXML
    private Button btnSend, btnMyProfile, btnLogout;
    @FXML
    private TextArea textASendText;
    @FXML
    private CheckBox checkTranslate;
    @FXML
    private TabPane tabPaneChat, tabPContacts;
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
    private Label labelUserName, labelAbout, labelGroupName;
    @FXML
    private VBox centerVbox, vBoxInfo;

    //Message broadcast
    private ObservableList<Message> broadcastObservableList;
    private ObservableList<Profile> profilesObservableList;
    private ObservableList<Profile> contactsObservableList;
    //FriendList

    //Group Contact (Left in Gui)
    private ObservableList<Group> groupsObservableList;
    private ObservableList<Profile> participantsObservableList;

	private Menu addToGroupChatContacts;

	@FXML
	private void initialize() {
		/**Basic GUI initialize + Listener **/
		addListener();
		/**get Instance of Client **/
        client=Client.getInstance(SERVER_HOST, SERVER_PORT);
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
                    Profile c=p.clone();
                    c.setStatus(-1);
                    participantsObservableList.add(c);
				}
			}else{
			    participantsObservableList.clear();
            }
        });
        rootPane.setRight(null);
        this.initializeNotificationHandling();
        tabPContacts.tabMinWidthProperty().bind(tabPContacts.prefWidthProperty().subtract(90).divide(tabPContacts.getTabs().size()));

        client.fetchAndShowLocalMessageStore();
    }

    private void initializeNotificationHandling() {
        this.tabOnlineListView.setOnMouseClicked(e -> {
            if(e.getClickCount()==2) {
                createNewChatTab(this.tabOnlineListView.getSelectionModel().getSelectedItem().getUserName());
            }
        });

        this.textASendText.setOnMouseClicked(ev -> this.removeNotification());

        this.tabPaneChat.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
                Tab selectedTab=tabPaneChat.getTabs().get(newValue.intValue());
                if(selectedTab!=null && selectedTab.getStyleClass().contains("tab-notification")) {
                    selectedTab.getStyleClass().remove("tab-notification");
                }
            }
        });
    }

    /**
     * adds listener to gui
     **/
    private void addListener() {
        btnSend.setOnMouseClicked(ev -> onSendClicked());
        textASendText.setOnKeyReleased(event -> {
            if(event.getCode()==KeyCode.ENTER) {
                onSendClicked();
            }
        });
    }

    /**
     * initializes Chat ListView with 1 tab for broadcast
     **/
    private void initializeChatWindow() {
        broadcastObservableList=FXCollections.observableArrayList();
        listViewBroadcast.setItems(broadcastObservableList);
        listViewBroadcast.setCellFactory(messagesListView -> new ChatListViewCell(listViewBroadcast.prefWidthProperty()));
        tabPaneChat.getTabs().get(0).setId(BROADCAST);
    }

    private void initializeTabPaneUsers() {
        /**display online Profiles initialize **/
        profilesObservableList=FXCollections.observableArrayList();
        tabOnlineListView.setItems(profilesObservableList);
        tabOnlineListView.setCellFactory(profilesListView -> new ContactListViewCell(tabOnlineListView.prefWidthProperty()));
        /**display contacts Profiles initialize**/
        contactsObservableList=FXCollections.observableArrayList();
        tabContactsListView.setItems(contactsObservableList);
        tabContactsListView.setCellFactory(profileListView -> new ContactListViewCell(tabContactsListView.prefWidthProperty()));
        /**display groups initialize**/
        groupsObservableList=FXCollections.observableArrayList();
        tabGroupChatListView.setItems(groupsObservableList);
        tabGroupChatListView.setCellFactory(groupsListView -> new GroupChatListViewCell());
        /** add double click listener to tabs**/
        tabOnlineListView.setOnMouseClicked(e -> {
            if(e.getClickCount()==2) {
                createNewChatTab(tabOnlineListView.getSelectionModel().getSelectedItem().getUserName());
            }
        });
        tabContactsListView.setOnMouseClicked(e -> {
            if(e.getClickCount()==2) {
                createNewChatTab(tabContactsListView.getSelectionModel().getSelectedItem().getUserName());
            }
        });
        tabGroupChatListView.setOnMouseClicked(e -> {
            if(e.getClickCount()==2) {
                createNewChatTab(tabGroupChatListView.getSelectionModel().getSelectedItem().getName());
            }
        });

        /**Online List Context Menu**/
        //TODO Add to Group Chat, Show Profile, RemoveFriend
        ContextMenu contextMenuOnlineUsers=new ContextMenu();
        contextMenuOnlineUsers.setId("OnlineUsersContextMenu");
        MenuItem addFriend=new MenuItem("Add as Friend");
        addFriend.setGraphic(FXUtil.generateIcon(FontAwesomeIcon.USER_PLUS));
        MenuItem createChat=new MenuItem("Create Chat");
        createChat.setGraphic(FXUtil.generateIcon(FontAwesomeIcon.ALIGN_JUSTIFY));
        MenuItem showProfile=new MenuItem("Show Profile");
        showProfile.setGraphic(FXUtil.generateIcon(FontAwesomeIcon.USER_SECRET));
        setOnShowPublicProfile(showProfile, tabOnlineListView.getSelectionModel());
        addFriend.setOnAction(e ->
                addFriend(tabOnlineListView.getSelectionModel().getSelectedItem()));
        createChat.setOnAction(e ->
                createNewChatTab(tabOnlineListView.getSelectionModel().getSelectedItem().getUserName()));
        contextMenuOnlineUsers.getItems().addAll(addFriend, createChat, showProfile);
        tabOnlineListView.setContextMenu(contextMenuOnlineUsers);

        /**Contact List Context Menu**/
        ContextMenu contextMenuContacts=new ContextMenu();
        contextMenuContacts.setId("ContactsContextMenu");
        MenuItem createChat1=new MenuItem("Create Chat");
        createChat1.setGraphic(FXUtil.generateIcon(FontAwesomeIcon.ALIGN_JUSTIFY));
        createChat1.setOnAction(e ->
                createNewChatTab(tabContactsListView.getSelectionModel().getSelectedItem().getUserName()));
        addToGroupChatContacts=new Menu("Add to Group Chat");
        addToGroupChatContacts.setGraphic(FXUtil.generateIcon(FontAwesomeIcon.USERS));
        MenuItem showProfile1=new MenuItem("Show Profile");
        showProfile1.setGraphic(FXUtil.generateIcon(FontAwesomeIcon.USER_SECRET));
        setOnShowPublicProfile(showProfile1, tabContactsListView.getSelectionModel());
        MenuItem removeFriend=new MenuItem("Remove Friend");
        removeFriend.setGraphic(FXUtil.generateIcon(FontAwesomeIcon.USER_TIMES));

        contextMenuContacts.getItems().addAll(createChat1, addToGroupChatContacts, showProfile1, removeFriend);
        tabContactsListView.setContextMenu(contextMenuContacts);
    }

    private void setOnShowPublicProfile(final MenuItem showProfile1, final MultipleSelectionModel<Profile> selectionModel) {
        showProfile1.setOnAction(e -> {
                    client.openWindow(Constants.Windows.PUBLIC_PROFILE);
                    Platform.runLater(() -> {
                        PublicProfileController publicProfileController=(PublicProfileController) client.peekController();
                        publicProfileController.setProfile(selectionModel.getSelectedItem());
                    });
                }
        );
    }

    private void removeNotification() {
        String selectedUserName=this.tabPaneChat.getSelectionModel().getSelectedItem().getId();
        Optional<Tab> selectedTabPane=this.tabPaneChat.getTabs().stream().filter(tab -> tab.getText().toLowerCase().equals(selectedUserName.toLowerCase())).findFirst();

        selectedTabPane.ifPresent(tab -> {
            if(tab.getStyleClass().contains("tab-notification")) {
                tab.getStyleClass().remove("tab-notification");
            }
        });
    }

    private void onLogoutClicked() {
        client.sendToServer(LOGOFF, client.getAccount());
        client.setAccount(null);
        client.closeCurrentWindowNoExit();
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
            imgAvatar.setImage(FXUtil.getDefaultAvatarMin());
        }
    }

    private void addFriend(Profile selectedUser) {
        client.sendToServer(ADD_FRIEND, selectedUser);
    }

    /**
     * send text in text area to server as a message and displays the text in the currently active chat window
     **/
    private void onSendClicked() {
        if(!textASendText.getText().isEmpty()) {
            Profile selectedUser=tabOnlineListView.getSelectionModel().getSelectedItem();
            String receiver=tabPaneChat.getSelectionModel().getSelectedItem().getId();
            //String receiver = selectedUser==null? "broadcast" : selectedUser.getUserName();
            Message newMessage=new Message(checkTranslate.isSelected(), textASendText.getText(),
                    new Timestamp(System.currentTimeMillis()), client.getAccount().getUserName(), receiver);
            //Display new message in chat
            ListView<Message> lv=(ListView<Message>) tabPaneChat.getSelectionModel().getSelectedItem().getContent();
            lv.getItems().add(newMessage);
            lv.scrollTo(lv.getItems().size() - 1);
            textASendText.clear();
            client.sendToServer(MESSAGE_SENT, newMessage);
            client.storeMessageLocal(newMessage);
        }
        else {

        }
    }

    /**
     * for displaying received messages, one messages are displayed via onSendClicked method
     **/
    public void displayNewMessage(Message message) {
        Tab displayTab=null;
        if(message.getReceiver().equals(BROADCAST) || message.getReceiver().startsWith("#")) {
            displayTab=createNewChatTab(message.getReceiver());
        }
        else {
            /** If logged user is sender, show in receiver Tab*/
            if(message.getSender().equals(client.getAccount().getUserName())) {
                displayTab=createNewChatTab(message.getReceiver());
            }
            else {
                displayTab=createNewChatTab(message.getSender());
            }
        }
        //get ListView from tab to add text
        ListView<Message> lv=(ListView<Message>) displayTab.getContent();
        lv.getItems().add(message);
        displayTab.getStyleClass().add("tab-notification");
        lv.scrollTo(lv.getItems().size() - 1);
    }

    public void displayOnlineProfiles(ArrayList<Profile> profiles) {
        profilesObservableList.clear();
        profilesObservableList.addAll(profiles);
        /** Update status/profiles of contacts*/
        for(int i=0; i<contactsObservableList.size(); i++) {
            Profile contact=contactsObservableList.get(i);
            Optional<Profile> profile=profiles.stream().filter(p -> p.getUserName().equals(contact.getUserName())).findFirst();
            if(profile.isPresent()) {
                contactsObservableList.set(i, profile.get());
            }
            else {
                contact.setStatus(0);
                contactsObservableList.set(i, contact);
            }

        }
    }

    public void displayContactsProfiles(ArrayList<Profile> profiles) {
        contactsObservableList.clear();
        contactsObservableList.addAll(profiles);
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
                    Profile c=p.clone();
                    c.setStatus(-1);
                    participantsObservableList.add(c);
				}
                rootPane.setRight(vBoxInfo);
                labelGroupName.setText(g.getName());
			} else {
				participantsObservableList.clear();
                rootPane.setRight(null);
			}
		});
	}

	public void fillContextMenuAddToGroup(ArrayList<Group> groups) {
		//addToGroupChat.getItems().clear();
		addToGroupChatContacts.getItems().clear();
		MenuItem createNewGroup = new MenuItem("Create New Group");
		addToGroupChatContacts.getItems().add(createNewGroup);
		createNewGroup.setOnAction(e ->
                createNewGroup(showNewGroupDialog(), tabContactsListView.getSelectionModel().getSelectedItem()));
		for (Group g : groups) {
			MenuItem group = new MenuItem(g.getName());
			group.setOnAction(ev -> {
				System.out.println("Clicked on " + g.getName());
				System.out.println("On profile " + tabContactsListView.getSelectionModel().getSelectedItem());
				Group myGroup = groups.stream().filter(gr -> gr.getName().equals(group.getText())).findFirst().get();
				myGroup.addParticipant(tabContactsListView.getSelectionModel().getSelectedItem());
				client.sendToServer(GROUP_UPDATE, myGroup);
			});
			//addToGroupChat.getItems().add(group);
			addToGroupChatContacts.getItems().add(group);
		}
	}

	public void onMyProfileClicked() {
        client.openWindow(Constants.Windows.USER_PROFILE);
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
     * Shows new Group Dialog and returns name of new Group
     */
    private String showNewGroupDialog() {
		Stage window = new Stage();
		window.initModality(Modality.APPLICATION_MODAL);
		window.setMinWidth(250);
        window.setTitle("Enter new Group name");
        window.initStyle(StageStyle.UNDECORATED);
        window.getIcons().add(FXUtil.getDefaultIcon());

		TextField textField = new TextField();
		Button yButton = new Button("Create");
        yButton.getStyleClass().add("green");
		Button nbButton = new Button("Cancel");
        nbButton.getStyleClass().add("orange");
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
        buttons.getChildren().addAll(nbButton,yButton);
		layout.getChildren().addAll(textField, buttons);
		layout.setAlignment(Pos.CENTER);

        VBox wrapBox=new VBox();
        wrapBox.setId("window-wrapper");
        wrapBox.getStylesheets().add(getClass().getResource("../resources/main.css").toExternalForm());
        FXMLLoader titleBarLoader=new FXMLLoader(getClass().getResource("../TitleBar.fxml"));
        TitleBarController titleBarController=new TitleBarController();
        titleBarController.setStage(window);
        titleBarLoader.setController(titleBarController);

        HBox titleBar=null;
        try {
            titleBar=titleBarLoader.load();
            titleBar.prefWidthProperty().bind(layout.prefWidthProperty());
            wrapBox.getChildren().addAll(titleBar, layout);
            Scene scene=new Scene(wrapBox);
            window.setScene(scene);
            Platform.runLater(() -> textField.requestFocus());
            window.showAndWait();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

		return exitCode.get();
	}

    private void createNewGroup(String groupName, Profile firstMember) {
        if(groupName==null) {
            return;
        }
        System.out.println(groupName);
        groupName = "#" + groupName;
        Group newGroup = new Group(groupName);
        newGroup.addParticipant(client.getAccount().getProfile());
        newGroup.addParticipant(firstMember);
        client.sendToServer(GROUP_UPDATE, newGroup);
    }

}
