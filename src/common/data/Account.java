package common.data;


public class Account extends ContactInfo {
    int aid;
    private String password;

    public Account(){
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password=password;
    }

    public ContactInfo getContactInfo(){
        return new ContactInfo(userName,status,aboutMe,profilePicture);
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
