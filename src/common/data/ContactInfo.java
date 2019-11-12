package common.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "_class")
public class ContactInfo extends Account {

    public ContactInfo(int aid, String userName, String password, int status, String aboutMe, String profilePicture) {
        super(aid, userName, password, status, aboutMe, profilePicture);
    }
}
