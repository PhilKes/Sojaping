package common.data;

import common.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AccountBuilder {
    private int aid;
    private String userName;
    private String password;
    private int status;
    private String aboutMe;
    private String profilePicture;
    private List<String> languages=new ArrayList<>(Arrays.asList(Constants.Translation.ENGLISH_VALUE));

    public AccountBuilder setAid(int aid) {
        this.aid=aid;
        return this;
    }

    public AccountBuilder setUserName(String userName) {
        this.userName=userName;
        return this;
    }

    public AccountBuilder setPassword(String password) {
        this.password=password;
        return this;
    }

    public AccountBuilder setStatus(int status) {
        this.status=status;
        return this;
    }

    public AccountBuilder setAboutMe(String aboutMe) {
        this.aboutMe=aboutMe;
        return this;
    }

    public AccountBuilder setProfilePicture(String profilePicture) {
        this.profilePicture=profilePicture;
        return this;
    }

    public AccountBuilder setLanguages(List<String> languages) {
        this.languages=languages;
        return this;
    }

    public Account createAccount() {
        return new Account(aid, userName, password, status, aboutMe, profilePicture, languages);
    }
}