package common.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Profile {
    protected String userName;
    protected int status;
    protected String aboutMe;
    protected String profilePicture;
    protected List<String> languages;
    protected boolean blocked;

    public Profile() {
        languages=new ArrayList<>();
    }
    public Profile(String userName, int status, String aboutMe, String profilePicture, List<String> languages) {
        this.userName=userName;
        this.status=status;
        this.aboutMe=aboutMe;
        this.profilePicture=profilePicture;
        this.languages=languages;
        this.blocked = false;
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

    public List<String> getLanguages() {
        return languages;
    }
    public void setLanguages(List<String> languages) {
        this.languages=languages;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @Override
    public boolean equals(Object o) {
        if(this==o) {
            return true;
        }
        if(o==null || getClass()!=o.getClass()) {
            return false;
        }
        Profile profile=(Profile) o;
        return status==profile.status &&
                Objects.equals(userName, profile.userName) &&
                Objects.equals(aboutMe, profile.aboutMe) &&
                Objects.equals(profilePicture, profile.profilePicture) &&
                Objects.equals(languages, profile.languages);
    }

    @Override
    public Profile clone() {
        List<String> langs=new ArrayList<>(languages);
        return new Profile(userName, status, aboutMe, profilePicture, langs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, status, aboutMe, profilePicture, languages);
    }

    @Override
    public String toString() {
        return "Profile{" +
                "userName='" + userName + '\'' +
                ", status=" + status +
                ", aboutMe='" + aboutMe + '\'' +
                ", languages=" + languages +
                '}';
    }
}
