package common.data;

public class ContactInfo  {
    protected String userName;
    protected int status;
    protected String aboutMe;
    protected String profilePicture;
    //TODO do not extend Account, only store userName, status,aboutMe,profilePicture


    public ContactInfo() {
    }

    public ContactInfo(String userName, int status, String aboutMe, String profilePicture) {
        this.userName=userName;
        this.status=status;
        this.aboutMe=aboutMe;
        this.profilePicture=profilePicture;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName=userName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status=status;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe=aboutMe;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture=profilePicture;
    }

    @Override
    public String toString() {
        return userName+"{" +
                " status=" + status +
                ", aboutMe='" + aboutMe + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                '}';
    }
}
