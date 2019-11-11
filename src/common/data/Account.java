package common.data;

public class Account {
    int aid;
    String userName;
    int status;
    String aboutMe;
    String profilePicture;

    public void setAid(int aid) {
        this.aid = aid;
    }

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

    public int getAid() {
        return aid;
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
