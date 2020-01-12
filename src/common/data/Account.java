package common.data;


import java.util.List;

public class Account extends Profile {
    int aid=-1;
    private String password;

    public Account() {
        this.profilePicture="";
    }

    public Account(int aid, String userName, String password, int status, String aboutMe, String profilePicture, List<String> languages) {
        this.aid=aid;
        this.userName=userName;
        this.password=password;
        this.status=status;
        this.aboutMe=aboutMe;
        if(profilePicture!=null) {
            this.profilePicture=profilePicture;
        }
        else {
            this.profilePicture="";
        }
        this.languages=languages;
    }

    public int getAid() {
        return aid;
    }

    public void setAid(int aid) {
        this.aid=aid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password=password;
    }

    public Profile getProfile() {
        return new Profile(userName, status, aboutMe, profilePicture, languages);
    }

    @Override
    public String toString() {
        return "Account{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", status=" + status +
                ", aboutMe='" + aboutMe + '\'' +
                ", languages=" + languages +
                '}';
    }

    public boolean equals(Account acc) {
        return this.aid==acc.getAid() && this.userName.equals(acc.getUserName())
                && this.password.equals(acc.getPassword()) && this.status==acc.getStatus()
                && this.aboutMe.equals(acc.getAboutMe())
                && ((this.profilePicture==null && acc.getProfilePicture()==null) || this.profilePicture.equals(acc.getProfilePicture()));
    }
}
