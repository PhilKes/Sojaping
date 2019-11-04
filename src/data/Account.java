package data;

public class Account {
    String userName;
    int status;
    String aboutMe;
    String profilePicture;

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getUserName() {
        return userName;
    }

    public int getStatus() {
        return status;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public String getProfilePicture() {
        return profilePicture;
    }
}
