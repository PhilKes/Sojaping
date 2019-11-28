package client.presentation;

import common.Util;
import common.data.Profile;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class PublicProfileController {
    @FXML
    private Label labelAbout, labelUserName;
    @FXML
    private ImageView imgAvatar;

    private Profile profile;

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
}
