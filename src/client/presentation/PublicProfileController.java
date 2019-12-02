package client.presentation;

import common.Util;
import common.data.Profile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class PublicProfileController extends UIController {
    @FXML
    private Label labelAbout, labelUserName;
    @FXML
    private ImageView imgAvatar;
    @FXML
    private Button btnAddFriend;

    private Profile profile;

    @FXML
    private void initialize() {
        btnAddFriend.setOnMouseClicked(e -> onAddFriendClicked());
    }

    private void onAddFriendClicked() {
        //TODO SAVE TO CONTACTS client.sendToServer(CONTACT_ADD,profile);
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile=profile;
        loadProfile();
    }

    private void loadProfile() {
        labelUserName.setText(profile.getUserName());
        labelAbout.setText(profile.getAboutMe());
        //TODO LOAD AVATAR FROM DB
        if(profile.getProfilePicture()==null) {
            imgAvatar.setImage(Util.getDefaultAvatar());
        }
    }

    @Override
    public void close() {
        Platform.runLater(() -> ((Stage) labelUserName.getScene().getWindow()).close());
    }
}
