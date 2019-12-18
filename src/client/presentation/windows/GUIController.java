package client.presentation.windows;

import client.Client;
import client.presentation.FXUtil;
import client.presentation.RichTextArea;
import client.presentation.TitleBarController;
import client.presentation.UIControllerWithInfo;
import client.presentation.genericarea.LinkedImage;
import client.presentation.genericarea.ParStyle;
import client.presentation.genericarea.TextStyle;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.GenericStyledArea;
import org.reactfx.util.Either;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static common.Constants.Contexts.*;
import static common.Constants.SERVER_HOST;
import static common.Constants.SERVER_PORT;

public class GUIController extends UIControllerWithInfo {
    @FXML
    private BorderPane rootPane;
    @FXML
    private Button btnSend, btnMyProfile, btnLogout;

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
    private ImageView imgAvatar, imageLogo;
    @FXML
    private Label labelUserName, labelAbout, labelGroupName;
    @FXML
    private VBox centerVbox, vBoxInfo;
    @FXML
    private CheckBox checkSmileys;
    @FXML
    private HBox hBoxSmileys, hBoxTextArea;
    @FXML
    private ScrollPane scrollSmileys;

    //Message broadcast
    private ObservableList<Message> broadcastObservableList;
    private ObservableList<Profile> profilesObservableList;
    private ObservableList<Profile> contactsObservableList;
    //FriendList

    //Group Contact (Left in Gui)
    private ObservableList<Group> groupsObservableList;
    private ObservableList<Profile> participantsObservableList;

    private Menu addToGroupChatContacts;
    private MenuItem blockFriend;
    private ContextMenu contextMenuOnlineUsers;

    private RichTextArea richTextArea;

    private Group currentSelectedGroup;

    @FXML
    private HBox hBoxAvatar;

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
        participantsObservableList=FXCollections.observableArrayList();
        listVInfo.setItems(participantsObservableList);
        listVInfo.setCellFactory(profilesListView -> new ContactListViewCell(listVInfo.prefWidthProperty()));
        /**Group List Context Menu**/
        btnLogout.setOnMouseClicked(e -> onLogoutClicked());
        labelAbout.setWrapText(true);
        VBox.setVgrow(tabPaneChat, Priority.ALWAYS);
        loadAccount(client.getAccount());
        tabPaneChat.getSelectionModel().selectedItemProperty().addListener((observable, tabOld, tabNew) -> {
            if(tabNew.getId().startsWith("#")) {
                Group g=groupsObservableList.stream().filter(group -> tabNew.getId().equals(group.getName())).findFirst().get();
                this.currentSelectedGroup = g;
                // TODO Group Picture - set picture from current group from DB in imageView
                FXUtil.setBase64PicInImageView(imageLogo, this.currentSelectedGroup.getGroupPicture(), true);
                participantsObservableList.clear();
                for(Profile p : g.getParticipants()) {
                    Profile c=p.clone();
                    c.setStatus(-1);
                    participantsObservableList.add(c);
                }
            }
            else {
                participantsObservableList.clear();
            }
        });
        rootPane.setRight(null);
        tabPContacts.tabMinWidthProperty().bind(tabPContacts.prefWidthProperty().subtract(90).divide(tabPContacts.getTabs().size()));

        initializeNotificationHandling();
        initSendRichTextArea();
        /** Load locally stored Messages */
        client.fetchAndShowLocalMessageStore();
        /** Fetch missed messages from Server*/
        client.sendToServer(MESSAGE_FETCH, null);
        /** Handle group picture*/
        imageLogo.setOnMouseClicked(ev -> changeGroupPicture());
    }

    private void changeGroupPicture() {
        try {
            String base64Picture = FXUtil.uploadPictureViaFileChooser(this.stage, this.imageLogo);
            this.currentSelectedGroup.setGroupPicture(base64Picture);
            this.client.sendToServer(GROUP_UPDATE, this.currentSelectedGroup);
        } catch (Exception e) {
            showInfo(e.getMessage(), InfoType.ERROR);
        }
    }

    /**
     * Init bottom RichTextArea for send text
     */
    private void initSendRichTextArea() {
        hBoxSmileys.getChildren().clear();
        /** Show/Hide smiley bar*/
        checkSmileys.selectedProperty().addListener((old, v, newV) -> {
            if (old.getValue()) {
                hBoxSmileys.getChildren().add(scrollSmileys);
                checkSmileys.setPrefHeight(FXUtil.SMILEY_BAR_HEIGHT);
            } else {
                hBoxSmileys.getChildren().clear();
                checkSmileys.setPrefHeight(20.0);
            }
            Platform.runLater(() -> richTextArea.getArea().requestFocus());
        });
        richTextArea = new RichTextArea();
        VirtualizedScrollPane<GenericStyledArea<ParStyle, Either<String, LinkedImage>, TextStyle>> vsPane = new VirtualizedScrollPane<>(richTextArea.getArea());
        vsPane.prefWidthProperty().bind(hBoxTextArea.prefWidthProperty());
        vsPane.prefHeightProperty().bind(hBoxTextArea.prefWidthProperty());
        vsPane.getStyleClass().add("text-area");
        hBoxTextArea.getChildren().add(vsPane);
        fillSmileysGrid(richTextArea);
    }

    private void initializeNotificationHandling() {
        this.tabOnlineListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                createNewChatTab(this.tabOnlineListView.getSelectionModel().getSelectedItem().getUserName());
            }
        });

        this.hBoxTextArea.setOnMouseClicked(ev -> this.removeNotification());
        this.tabPaneChat.getSelectionModel().selectedIndexProperty().addListener((ov, oldValue, newValue) -> {
            Tab selectedTab = tabPaneChat.getTabs().get(newValue.intValue());
            if (selectedTab != null && selectedTab.getStyleClass().contains("tab-notification")) {
                selectedTab.getStyleClass().remove("tab-notification");
            }
        });
    }

    /**
     * adds listener to gui
     **/
    private void addListener() {
        btnSend.setOnMouseClicked(ev -> onSendClicked());
        hBoxTextArea.setOnKeyReleased(event -> {
            if(event.getCode()==KeyCode.ENTER) {
                onSendClicked();
            }
        });
    }

    /**
     * Fill smileys images into Grid
     * Smileys from: https://www.flaticon.com/packs/smileys-3
     */
    private void fillSmileysGrid(RichTextArea textAreaSend) {
        //scrollSmileys.setContent(null);
        scrollSmileys.setMaxHeight(FXUtil.SMILEY_BAR_HEIGHT);
        scrollSmileys.setMinHeight(FXUtil.SMILEY_BAR_HEIGHT);
        scrollSmileys.setPrefHeight(FXUtil.SMILEY_BAR_HEIGHT);
        scrollSmileys.setMaxWidth(FXUtil.SMILEY_BAR_WIDTH);
        scrollSmileys.setPrefWidth(FXUtil.SMILEY_BAR_WIDTH);
        scrollSmileys.setMinWidth(FXUtil.SMILEY_BAR_WIDTH);

        scrollSmileys.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        GridPane smileyGrid=new GridPane();
        smileyGrid.setPrefWidth(FXUtil.SMILEY_BAR_WIDTH);
        smileyGrid.setMaxWidth(FXUtil.SMILEY_BAR_WIDTH);
        smileyGrid.setMinWidth(FXUtil.SMILEY_BAR_WIDTH);
        /** Fill gridPane with 20 columns, load smiley images with click listenersT*/
        int columns=20;
        for(int i=0; i<columns; i++) {
            ColumnConstraints columnConstraints=new ColumnConstraints();
            columnConstraints.setPercentWidth(100.0 / columns);
            smileyGrid.getColumnConstraints().add(columnConstraints);
        }
        List<String> smileyPaths=FXUtil.getSmileyImagePaths();
        for(int y=0; y<(smileyPaths.size() / columns) + (smileyPaths.size() % columns>0 ? 1 : 0); y++) {
            RowConstraints rowConstraints=new RowConstraints();
            rowConstraints.setMinHeight(22);
            rowConstraints.setPrefHeight(22);
            smileyGrid.getRowConstraints().add(rowConstraints);
            for(int x=0; x<columns; x++) {
                if(y * columns + x >= smileyPaths.size()) {
                    break;
                }
                ImageView imgSmiley=new ImageView(FXUtil.getSmileyImage(y * columns + x));
                int finalI=y * columns + x;
                imgSmiley.setOnMouseClicked(ev -> {
                    textAreaSend.insertImage(smileyPaths.get(finalI));
                    Platform.runLater(() -> richTextArea.getArea().requestFocus());
                });
                smileyGrid.add(imgSmiley, x, y);
            }
        }
        scrollSmileys.setContent(smileyGrid);
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
        contextMenuOnlineUsers=new ContextMenu();
        contextMenuOnlineUsers.setId("OnlineUsersContextMenu");
        MenuItem addFriend=new MenuItem("Add as Friend");
        addFriend.setGraphic(FXUtil.generateIcon(FontAwesomeIcon.USER_PLUS));
        blockFriend=new MenuItem("Block");
        blockFriend.setGraphic(FXUtil.generateIcon(FontAwesomeIcon.USER_MD));
        blockFriend.setOnAction(e ->
                blockUser(tabOnlineListView.getSelectionModel().getSelectedItem(), true));
        MenuItem createChat=new MenuItem("Open Chat");
        createChat.setGraphic(FXUtil.generateIcon(FontAwesomeIcon.ALIGN_JUSTIFY));
        MenuItem showProfile=new MenuItem("Show Profile");
        showProfile.setGraphic(FXUtil.generateIcon(FontAwesomeIcon.USER_SECRET));
        setOnShowPublicProfile(showProfile, tabOnlineListView.getSelectionModel());
        addFriend.setOnAction(e ->
                addFriend(tabOnlineListView.getSelectionModel().getSelectedItem()));
        createChat.setOnAction(e ->
                createNewChatTab(tabOnlineListView.getSelectionModel().getSelectedItem().getUserName()));
        contextMenuOnlineUsers.getItems().addAll(addFriend, createChat, showProfile, blockFriend);
        tabOnlineListView.setContextMenu(contextMenuOnlineUsers);

        /**Contact List Context Menu**/
        ContextMenu contextMenuContacts=new ContextMenu();
        contextMenuContacts.setId("ContactsContextMenu");
        MenuItem createChat1=new MenuItem("Open Chat");
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
        removeFriend.setOnAction(e ->
                removeFriend(tabContactsListView.getSelectionModel().getSelectedItem()));
        contextMenuContacts.getItems().addAll(createChat1, addToGroupChatContacts, showProfile1, removeFriend);
        tabContactsListView.setContextMenu(contextMenuContacts);
    }

    private void blockUser(Profile blockedProfile, boolean block) {
        if (block) {
            client.sendToServer(BLOCK, blockedProfile);
        } else {
            client.sendToServer(UNBLOCK, blockedProfile);
        }
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

    private void onLogoutClicked() {
        client.logout();
    }

    /**
     * Load Account into My Profile (Top left)
     */
    public void loadAccount(Account acc) {
        labelUserName.setText(acc.getUserName());
        labelAbout.setText(acc.getAboutMe());
        FXUtil.setBase64PicInImageView(imgAvatar, acc.getProfilePicture());
    }

    private void addFriend(Profile selectedUser) {
        client.sendToServer(ADD_FRIEND, selectedUser);
    }

    private void removeFriend(Profile contact) {
        client.sendToServer(REMOVE_FRIEND, contact);
    }

    /**
     * send text in text area to server as a message and displays the text in the currently active chat window
     **/
    private void onSendClicked() {
        StringBuilder stringBuilder=new StringBuilder();
        if(richTextArea.getArea().getLength()>1) {
            richTextArea.getArea().getParagraphs().forEach(list -> {
                for(Either<String, LinkedImage> item : list.getSegments()) {
                    /** If is String */
                    item.ifLeft(str -> {
                        System.out.println(str);
                        stringBuilder.append(str);
                    });
                    /** If is smiley get number of smiley */
                    Pattern numberPattern=Pattern.compile("/(\\d*?)-", Pattern.DOTALL);
                    item.ifRight(img -> {
                        Matcher m=numberPattern.matcher(img.getImagePath());
                        if(m.find()) {
                            String smileyNumber=m.group(1);
                            System.out.println("Smiley: " + smileyNumber);
                            stringBuilder.append("<i>").append(smileyNumber).append("</i>");
                        }
                    });
                }
            });
            System.out.println("Sent message: " + stringBuilder);
            String receiver=tabPaneChat.getSelectionModel().getSelectedItem().getId();
            Message newMessage=new Message(checkTranslate.isSelected(), stringBuilder.toString(),
                    new Timestamp(System.currentTimeMillis()), client.getAccount().getUserName(), receiver);
            ListView<Message> lv=(ListView<Message>) tabPaneChat.getSelectionModel().getSelectedItem().getContent();
            lv.getItems().add(newMessage);

            client.sendToServer(MESSAGE_SENT, newMessage);
            client.storeMessageLocal(newMessage);
            richTextArea.getArea().clear();
            checkSmileys.setSelected(false);
        }
       /* if(!textASendText.getText().isEmpty()) {
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

        }*/
    }

    public void onMyProfileClicked() {
        client.openWindow(Constants.Windows.USER_PROFILE);
    }

    /**
     * for displaying received messages, one messages are displayed via onSendClicked method
     **/
    public void displayNewMessage(Message message, boolean newMessage) {
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
        if(newMessage) {
            displayTab.getStyleClass().add("tab-notification");
        }
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
        List<Profile> blockedUsers=new ArrayList<>();
        profiles.forEach(profile -> {
            if(!profile.isBlocked()) {
                contactsObservableList.add(profile);
            }
            else {
                blockedUsers.add(profile);
            }
        });
        contextMenuOnlineUsers.setOnShown(ev -> fillBlockUnblockMenu(blockedUsers));
    }

    public void displayGroupChats(ArrayList<Group> groups) {
        groupsObservableList.clear();
        for(Group g : groups)
            groupsObservableList.add(g);
        tabPaneChat.getSelectionModel().selectedItemProperty().addListener((observable, tabOld, tabNew) -> {
            if(tabNew.getId().startsWith("#")) {
                Group g=groupsObservableList.stream().filter(group -> tabNew.getId().equals(group.getName())).findFirst().get();
                participantsObservableList.clear();
                for(Profile p : g.getParticipants()) {
                    Profile c=p.clone();
                    c.setStatus(-1);
                    participantsObservableList.add(c);
                }
                rootPane.setRight(vBoxInfo);
                labelGroupName.setText(g.getName());
            }
            else {
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
                Group.Participant participant=new Group.Participant(tabContactsListView.getSelectionModel().getSelectedItem());
                myGroup.addParticipant(participant);
                client.sendToServer(GROUP_UPDATE, myGroup);
            });
            //addToGroupChat.getItems().add(group);
            addToGroupChatContacts.getItems().add(group);
        }
    }

    public void fillBlockUnblockMenu(List<Profile> blockedUsers) {
        Profile profile = tabOnlineListView.getSelectionModel().getSelectedItem();
        if (profile == null)
            return;
        Optional<Profile> user = blockedUsers.stream().filter(b -> b.getUserName().equals(profile.getUserName())).findFirst();
        contextMenuOnlineUsers.getItems().remove(contextMenuOnlineUsers.getItems().size() - 1);
        if (user.isPresent()) {
            /** Show unblock*/
            blockFriend = new MenuItem("Unblock");
            blockFriend.setGraphic(FXUtil.generateIcon(FontAwesomeIcon.USER_PLUS));
            blockFriend.setOnAction(e ->
                    blockUser(user.get(), false));
        } else {
            /** show block*/
            blockFriend = new MenuItem("Block");
            blockFriend.setGraphic(FXUtil.generateIcon(FontAwesomeIcon.USER_MD));
            blockFriend.setOnAction(e ->
                    blockUser(profile, true));
        }
        contextMenuOnlineUsers.getItems().add(blockFriend);
    }

    /**
     * returns existing tab or create & returns new one
     **/
    private Tab createNewChatTab(String tabName) {
        Tab newTab=new Tab();
        newTab.setText(tabName);
        //Check if tab already exists then return
        for(Tab tab : tabPaneChat.getTabs()) {
            if(tab.getId().equals(tabName)) {
                tabPaneChat.getSelectionModel().select(tab);
                return tab;
            }
        }
        ObservableList<Message> newObservableList=FXCollections.observableArrayList();
        ListView<Message> newListView=new ListView<>();
        newListView.setItems(newObservableList);
        newListView.setCellFactory(messagesListView -> new ChatListViewCell(newListView.prefWidthProperty()));
        newTab.setContent(newListView);
        newTab.setId(tabName);
        tabPaneChat.getTabs().add(newTab);
        tabPaneChat.getSelectionModel().select(newTab);
        return newTab;
    }

    /**
     * Shows new Group Dialog and returns name of new Group
     */
    private String showNewGroupDialog() {
        Stage window=new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setMinWidth(150);
        window.setTitle("Enter new Group name");
        window.initStyle(StageStyle.UNDECORATED);
        window.getIcons().add(FXUtil.getDefaultIcon());

        TextField textField=new TextField();
        Button yButton=new Button("Create");
        yButton.getStyleClass().add("green");
        Button nbButton=new Button("Cancel");
        nbButton.getStyleClass().add("orange");
        AtomicReference<String> exitCode=new AtomicReference<>();
        yButton.setOnAction(e -> {
            exitCode.set(textField.getText());
            window.close();
        });
        nbButton.setOnAction(e -> {
            exitCode.set(null);
            window.close();
        });

        VBox layout = new VBox(5);
        layout.setStyle("-fx-padding: 8px;");
        HBox buttons = new HBox(200);
        buttons.getChildren().addAll(nbButton, yButton);
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
        groupName="#" + groupName;
        Group newGroup=new Group(groupName);
        Group.Participant founder=new Group.Participant(client.getAccount().getProfile());
        founder.setAdmin(true);
        newGroup.addParticipant(founder);
        Group.Participant participant=new Group.Participant(firstMember);
        newGroup.addParticipant(participant);
        client.sendToServer(GROUP_UPDATE, newGroup);
    }
}
