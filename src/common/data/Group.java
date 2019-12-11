package common.data;

import java.util.ArrayList;

public class Group {
    String name;
    String groupPicture;
    int groupID = -1;
    ArrayList<Profile> participants = new ArrayList<>();

    public Group() {
    }

    public Group(String name) {
        this.name = name;
        //addParticipant(profile);
        //participants.add(profile);
        groupPicture = "";
    }

    public void addParticipant(Profile profile){
        for(Profile p : participants){
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

    public ArrayList<Profile> getParticipants() {
        return participants;
    }

    public static void main(String[] args) {
        /*
        Profile a = new Profile("a", 0, "a", null);
        Profile b = new Profile("b", 0, "b", null);
        Group g = new Group("test", a);
        g.addParticipant(b);
        System.out.println(g);
        g.deleteParticipant(a);
        System.out.println(g);
        g.changeName("test2");
        System.out.println(g);
        */

    }
}
