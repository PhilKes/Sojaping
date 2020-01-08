package common.data;

import java.util.ArrayList;

public class Group {
    String name;
    String groupPicture;
    int groupID = -1;
    ArrayList<Participant> participants=new ArrayList<>();

    public Group() {
    }

    public Group(String name) {
        this.name = name;
        //addParticipant(profile);
        //participants.add(profile);
        groupPicture = "";
    }

    public void addParticipant(Participant profile) {
        for(Participant p : participants) {
            if(p.getUserName().equals(profile.getUserName())){
                return;
            }
        }
        participants.add(profile);
    }

    public void deleteParticipant(Profile profile){
        for(Profile p : participants){
            if(p.getUserName().equals(profile.getUserName())){
                participants.remove(p);
                return;
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getGroupPicture() {
        return groupPicture;
    }

    public void setGroupPicture(String pic) {
        this.groupPicture=pic;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public void changeName(String newName){
        this.name = newName;
    }
    @Override
    public String toString() {
        return "Group{" +
                "name='" + name + '\'' +
                ", participants=" + participants +
                '}';
    }

    public ArrayList<Participant> getParticipants() {
        return participants;
    }

    public static class Participant extends Profile {
        private boolean admin=false;

        public Participant() {
            this.admin=false;
        }

        public Participant(Profile profile) {
            super(profile.getUserName(), profile.status, profile.aboutMe, profile.profilePicture, profile.languages);
        }

        public boolean isAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin=admin;
        }

        @Override
        public String toString() {
            return "Participant{" +
                    "admin=" + admin +
                    ", userName='" + userName + '\'' +
                    '}';
        }
    }

}
