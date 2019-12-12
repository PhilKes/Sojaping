package client.presentation.windows;

import client.presentation.FXUtil;
import client.presentation.UIController;
import common.data.Profile;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import static common.Constants.Contexts.ADD_FRIEND;

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
        client.sendToServer(ADD_FRIEND, profile);
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
            imgAvatar.setImage(FXUtil.getDefaultAvatar());
        }
    }
}
