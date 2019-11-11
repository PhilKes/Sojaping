package common.data;

public class Account {
    int aid;
    String userName;
    private String password;
    int status;
    String aboutMe;
    String profilePicture;

    public Account (){

    }

    public Account(int aid, String userName, String password, int status, String aboutMe, String profilePicture) {
        this.aid = aid;
        this.userName = userName;
        this.password = password;
        this.status = status;
        this.aboutMe = aboutMe;
        this.profilePicture = profilePicture;
    }

    public int getAid() {
        return aid;
    }

    public void setAid(int aid) {
        this.aid = aid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    @Override
    public String toString() {
        return "Account{" +
                "aid=" + aid +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", status=" + status +
                ", aboutMe='" + aboutMe + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                '}';
    }

}
