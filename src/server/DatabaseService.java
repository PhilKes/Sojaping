package server;


import common.Util;
import common.data.*;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseService {

    public static class TableAccount {
        public static final String NAME="account";
        public static final String AID="aid";
        public static final String USERNAME="userName";
        public static final String PASSWORD="password";
        public static final String ABOUTME="aboutMe";
        public static final String PROFILEPICTURE="profilePicture";
        public static final String LANGUAGES="languages";
    }

    public static class TableContact {
        public static final String NAME="contact";
        public static final String CID="cid";
        public static final String AIDO="aido";
        public static final String AIDC="aidc";
    }

    public static class TableGroup {
        public static final String NAME = "groupChats";
        public static final String GID = "gid";
        public static final String GROUPNAME = "groupName";
        public static final String GROUPPICTURE = "groupPicture";
    }

    public static class TableParticipants {
        public static final String NAME = "participants";
        public static final String PID = "pid";
        public static final String IDGROUP = "idGroup";
        public static final String IDACCOUNT = "idAccount";
    }

    public static class TableMessages {
        public static final String NAME = "messages";
        public static final String SMID = "smid";
        public static final String AID = "aid";
        public static final String SENDER = "sender";
        public static final String RECEIVER = "receiver";
        public static final String TIMESTAMP = "timestamp";
        public static final String TEXT = "text";
    }
    private static final String SOJAPING="sojaping.db";
    public static String URL="";
    static int lastRow;

    public DatabaseService(String url) {
        URL="jdbc:sqlite:assets/" + url;
    }

    public static void createNewDatabase(String fileName) {
        String url="jdbc:sqlite:assets/" + fileName;
        try(Connection conn=DriverManager.getConnection(url)) {
            if(conn!=null) {
                DatabaseMetaData meta=conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Connection connect() {
        Connection conn=null;
        try {
            conn=DriverManager.getConnection(URL);
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void createNewTableAccount() {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS " + TableAccount.NAME + " (\n"
                + TableAccount.AID + " integer PRIMARY KEY autoincrement,\n"
                + TableAccount.USERNAME + " text NOT NULL UNIQUE,\n"
                + TableAccount.PASSWORD + " text NOT NULL,\n"
                //+ STATUS + " integer NOT NULL,\n"
                + TableAccount.ABOUTME + " text,\n"
                + TableAccount.PROFILEPICTURE + " text,\n"
                + TableAccount.LANGUAGES + " text NOT NULL"
                + ");";
        try(Connection conn=DriverManager.getConnection(URL);
            Statement stmt=conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createNewTableContactList() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TableContact.NAME + " (\n"
                + TableContact.CID + " integer PRIMARY KEY,\n"
                + TableContact.AIDO + " integer NOT NULL,\n" //aid of the "owner" of this list
                + TableContact.AIDC + " integer NOT NULL,\n"
                + "UNIQUE(" + TableContact.AIDO + "," + TableContact.AIDC + ")\n"
                + "FOREIGN KEY (" + TableContact.AIDO + ")\n"
                + "REFERENCES account(" + TableAccount.AID + ")\n"
                + "FOREIGN KEY (" + TableContact.AIDC + ")\n"
                + "REFERENCES account(" + TableAccount.AID + ")\n"
                + "ON DELETE CASCADE"
                + ");";
        try(Connection conn=DriverManager.getConnection(URL);
            Statement stmt=conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createNewTableGroup(){
        String sql = "CREATE TABLE IF NOT EXISTS " + TableGroup.NAME + " (\n"
                + TableGroup.GID + " integer PRIMARY KEY,\n"
                + TableGroup.GROUPNAME + " text NOT NULL UNIQUE,\n"
                + TableGroup.GROUPPICTURE + " text);";
        try(Connection conn=DriverManager.getConnection(URL);
            Statement stmt=conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        }
        catch(SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public static void createNewTableParticipants(){
        String sql = "CREATE TABLE IF NOT EXISTS " + TableParticipants.NAME + " (\n"
                + TableParticipants.PID + " integer PRIMARY KEY,\n"
                + TableParticipants.IDGROUP + " integer NOT NULL,\n"
                + TableParticipants.IDACCOUNT + " integer NOT NULL,\n"
                + "UNIQUE(" + TableParticipants.IDGROUP + "," + TableParticipants.IDACCOUNT + ")\n"
                + "FOREIGN KEY (" + TableParticipants.IDGROUP + ")\n"
                + "REFERENCES " + TableGroup.NAME + "(" + TableGroup.GID + ")\n"
                + "FOREIGN KEY (" + TableParticipants.IDACCOUNT + ")\n"
                + "REFERENCES account(" + TableAccount.AID + ")\n"
                + "ON DELETE CASCADE"
                + ");";
        try(Connection conn=DriverManager.getConnection(URL);
            Statement stmt=conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        }
        catch(SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public static void createNewTableMessages() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TableMessages.NAME + " (\n"
                + TableMessages.SMID + " integer PRIMARY KEY,\n"
                + TableMessages.AID + " integer NOT NULL,\n"
                + TableMessages.SENDER + " text NOT NULL,\n"
                + TableMessages.RECEIVER + " text NOT NULL,\n"
                + TableMessages.TIMESTAMP + " text NOT NULL,\n"
                + TableMessages.TEXT + " text NOT NULL,\n"
                + "FOREIGN KEY (" + TableMessages.AID + ")\n"
                + "REFERENCES " + TableAccount.NAME + "(" + TableAccount.AID + ")\n"
                + "ON DELETE CASCADE"
                + ");";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //This method is for debugging.
    public void selectAllAccounts() {
        String sql="SELECT * FROM account";
        try(Connection conn=this.connect();
            Statement stmt=conn.createStatement();
            ResultSet rs=stmt.executeQuery(sql)) {
            // loop through the result set
            while(rs.next()) {
                Account acc=new AccountBuilder().setAid(rs.getInt(TableAccount.AID)).setUserName(rs.getString(TableAccount.USERNAME))
                        .setPassword(rs.getString(TableAccount.PASSWORD)).setStatus(0)
                        .setAboutMe(rs.getString(TableAccount.ABOUTME)).setProfilePicture(rs.getString(TableAccount.PROFILEPICTURE))
                        .setLanguages(Util.joinedStringToList(rs.getString(TableAccount.LANGUAGES)))
                        .createAccount();
                System.out.println(acc);
            }
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Account Table
     */

    public void insertAccount(Account acc) throws Exception {
        String sql="INSERT INTO " + TableAccount.NAME + "(" + TableAccount.AID + ", " + TableAccount.USERNAME + ", " + TableAccount.PASSWORD + ", "
                + TableAccount.ABOUTME + ", " + TableAccount.PROFILEPICTURE + ", " + TableAccount.LANGUAGES + ") VALUES(NULL,?,?,?,?,?)";
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, acc.getUserName());
            pstmt.setString(2, acc.getPassword());
            //pstmt.setInt(3, acc.getStatus());
            pstmt.setString(3, acc.getAboutMe());
            pstmt.setString(4, acc.getProfilePicture());
            pstmt.setString(5, String.join(",", acc.getLanguages()));
            pstmt.executeUpdate();
            ResultSet rs=pstmt.getGeneratedKeys();
            if(rs.next()) {
                acc.setAid(rs.getInt(1));
                lastRow=rs.getInt(1);
                System.out.println("Inserted into DB:\t" + acc);
            }
        } catch(SQLException e) {
            if(e.getMessage().contains("[SQLITE_CONSTRAINT]  Abort due to constraint violation (UNIQUE constraint failed: account.userName)")) {
                throw new Exception("Username is already in use!");
            }
            e.printStackTrace();
        }
        String sql2="SELECT * FROM " + TableAccount.NAME + " WHERE userName = ?";
        try(Connection conn2=DriverManager.getConnection(DatabaseService.URL);
            PreparedStatement pstmt2=conn2.prepareStatement(sql2)) {
            pstmt2.setString(1, acc.getUserName());
            ResultSet rs=pstmt2.executeQuery();
            while(rs.next()) {
                acc.setAid(rs.getInt(TableAccount.AID));
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateAccount(Account acc) {
        String sql="UPDATE " + TableAccount.NAME + " SET " + TableAccount.USERNAME + " = ? , "
                + TableAccount.PASSWORD + " = ? , "
                + TableAccount.ABOUTME + " = ?, "
                + TableAccount.PROFILEPICTURE + " = ?, "
                + TableAccount.LANGUAGES + " = ? "
                + "WHERE " + TableAccount.AID + " = ?";
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql)) {
            pstmt.setString(1, acc.getUserName());
            pstmt.setString(2, acc.getPassword());
            pstmt.setString(3, acc.getAboutMe());
            pstmt.setString(4, acc.getProfilePicture());
            pstmt.setString(5, String.join(",", acc.getLanguages()));
            pstmt.setInt(6, acc.getAid());
            pstmt.executeUpdate();
            /** Debug */
            System.out.println("Updated: " + getAccountById(acc.getAid()));
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Account getAccountById(int aid) {
        return getAccountByArgument(Arrays.asList(new Pair<>(TableAccount.AID, aid)));
    }

    public Account getAccountByUsername(String userName) {
        return getAccountByArgument(Arrays.asList(new Pair<>(TableAccount.USERNAME, userName)));
    }

    public Account getAccountByLoginUser(LoginUser user) {
        return getAccountByArgument(Arrays.asList(new Pair<>(TableAccount.USERNAME, user.getUserName())
                , new Pair<>(TableAccount.PASSWORD, user.getPassword())));
    }

    /**
     * Selects Account based on WHERE's given by Arguments (Key,Value)-Pairs
     */
    public Account getAccountByArgument(List<Pair<String, ?>> pars) {
        StringBuilder builder=new StringBuilder("SELECT * FROM ").append(TableAccount.NAME);
        if(pars.size()>0) {
            builder.append(" WHERE ");
        }
        builder.append(String.join(" AND ", pars.stream().map(p -> p.getKey() + " = ?").collect(Collectors.toList())));
        String sql=builder.toString();
        Account acc=null;
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql)) {
            for(int i=0; i<pars.size(); i++) {
                pstmt.setObject(i + 1, pars.get(i).getValue());
            }
            ResultSet rs=pstmt.executeQuery();
            while(rs.next()) {
                acc=new AccountBuilder().setAid(rs.getInt(TableAccount.AID)).setUserName(rs.getString(TableAccount.USERNAME))
                        .setPassword(rs.getString(TableAccount.PASSWORD)).setStatus(0)
                        .setAboutMe(rs.getString(TableAccount.ABOUTME)).setProfilePicture(rs.getString(TableAccount.PROFILEPICTURE))
                        .setLanguages(Util.joinedStringToList(rs.getString(TableAccount.LANGUAGES)))
                        .createAccount();
            }
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return acc;
    }

    public Profile getProfileByAccountId(int aid) {
        return getAccountById(aid).getProfile();
    }

    public void deleteAccount(Account acc) {
        String sql="DELETE FROM " + TableAccount.NAME + " WHERE " + TableAccount.AID + " = ?";
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql)) {
            pstmt.setInt(1, acc.getAid());
            pstmt.executeUpdate();
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Contact Table */
    //This method is for debugging.
    public void selectAllContactsOfAccount(Account acc) {
        String sql="SELECT * FROM contact WHERE " + TableContact.AIDO + " = ?";
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql)) {
            pstmt.setInt(1, acc.getAid());
            ResultSet rs=pstmt.executeQuery();
            while(rs.next()) {
                System.out.println(rs.getInt(TableContact.CID) + "\t"
                        + rs.getInt(TableContact.AIDO) + "\t"
                        + rs.getInt(TableContact.AIDC) + "\t");
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public ArrayList<Profile> getAllContactsOfAccount(Account acc) {
        ArrayList<Profile> profiles=new ArrayList<>();
        String sql="SELECT " + TableContact.AIDC + " FROM contact WHERE " + TableContact.AIDO + " = ?";
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql)) {
            pstmt.setInt(1, acc.getAid());
            ResultSet rs=pstmt.executeQuery();
            while(rs.next()) {
                // contacts.add(new Contact(rs.getInt(TableContact.CID), rs.getInt(TableContact.AIDO), rs.getInt(TableContact.AIDC)));
                profiles.add(getProfileByAccountId(rs.getInt(TableContact.AIDC)));
            }
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return profiles;
    }

    public void insertContactOfAccount(Account account, Profile contact) throws Exception {
        String sql="INSERT INTO " + TableContact.NAME + "(" + TableContact.CID + ", " + TableContact.AIDO + ", "
                + TableContact.AIDC + ") VALUES(NULL,?,?)";
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, account.getAid());
            pstmt.setInt(2, getAccountByUsername(contact.getUserName()).getAid());
            pstmt.executeUpdate();
            ResultSet rs=pstmt.getGeneratedKeys();
            if(rs.next()) {
                System.out.println("Inserted into ContactList DB of Account " + account + ":\t" + contact);
            }
        }
        catch(SQLException e) {
            if(e.getMessage().contains("[SQLITE_CONSTRAINT]  Abort due to constraint violation (UNIQUE constraint failed: contact.aido, contact.aidc)")) {
                throw new Exception(contact.getUserName() + " is already in your contacts!");
            }
            //e.printStackTrace();
        }
    }

    /** Group Participant Table */

    public void insertParticipant(Group group, Profile participant) {
        int particpantID = this.getAccountByUsername(participant.getUserName()).getAid();
        String sql = "INSERT INTO " + TableParticipants.NAME + " (" + TableParticipants.PID + ", " + TableParticipants.IDGROUP + ", "
                + TableParticipants.IDACCOUNT + ") VALUES(NULL,?,?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, group.getGroupID());
            pstmt.setInt(2, particpantID);
            pstmt.executeUpdate();
            group.addParticipant(participant);
            System.out.println("Group after insert: " + group);
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Profile> getParticipants(Group group){
        ArrayList<Profile> participants = new ArrayList<>();
        String sql = "SELECT " + TableParticipants.IDACCOUNT + " FROM " + TableParticipants.NAME + " WHERE " + TableParticipants.IDGROUP + " = ?";
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql)) {
            pstmt.setInt(1, group.getGroupID());
            ResultSet rs=pstmt.executeQuery();
            while(rs.next()) {
                participants.add(getProfileByAccountId(rs.getInt(TableParticipants.IDACCOUNT)));
            }
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return participants;
    }

    public ArrayList<Profile> getParticipants(String groupName){
        ArrayList<Profile> participants = new ArrayList<>();
        String sql = "SELECT " + TableGroup.GID + " FROM " + TableGroup.NAME + " WHERE " + TableGroup.GROUPNAME + " = ?";
        int gid = -1;
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql)) {
            pstmt.setString(1, groupName);
            ResultSet rs=pstmt.executeQuery();
            while(rs.next()) {
                gid = rs.getInt(TableGroup.GID);
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        String sql2 = "SELECT " + TableParticipants.IDACCOUNT + " FROM " + TableParticipants.NAME + " WHERE " + TableParticipants.IDGROUP + " = ?";
        try(Connection conn2=this.connect();
            PreparedStatement pstmt2=conn2.prepareStatement(sql2)) {
            pstmt2.setInt(1, gid);
            ResultSet rs2=pstmt2.executeQuery();
            while(rs2.next()) {
                participants.add(getProfileByAccountId(rs2.getInt(TableParticipants.IDACCOUNT)));
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return participants;
    }

    /** Group Table */

    public ArrayList<Group> getMyGroups(Account acc){
        ArrayList<Group> groups = new ArrayList<>();
        String sql = "SELECT " + TableParticipants.IDGROUP + " FROM " + TableParticipants.NAME+" WHERE " +
                TableParticipants.IDACCOUNT + " = ?";
        String sql2 = "SELECT " + TableGroup.GROUPNAME + " FROM " + TableGroup.NAME+" WHERE " +
                TableGroup.GID + " = ?";
        List<Integer> groupIDs = new ArrayList<>();
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql)) {
            pstmt.setInt(1, acc.getAid());
            ResultSet rs=pstmt.executeQuery();

            while(rs.next()) {
                groupIDs.add(rs.getInt(TableParticipants.IDGROUP));
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        for(int i : groupIDs) {
            try (Connection conn2 = this.connect();
                 PreparedStatement pstmt2 = conn2.prepareStatement(sql2)) {
                pstmt2.setInt(1, i);
                ResultSet rs2 = pstmt2.executeQuery();
                while (rs2.next()) {
                    Group group = new Group(rs2.getString(TableGroup.GROUPNAME));
                    group.setGroupID(i);
                    //group.getParticipants().addAll(this.getParticipants(group));
                    groups.add(group);
                }
            } catch (SQLException f) {
                System.out.println(f.getMessage());
                f.printStackTrace();
            }
        }
        for(Group g : groups){
            g.getParticipants().addAll(this.getParticipants(g));
        }

        return groups;
    }

    public void insertGroup(Group group) throws Exception {
        String sql = "INSERT OR IGNORE INTO " + TableGroup.NAME + " (" + TableGroup.GID + ", " + TableGroup.GROUPNAME + ", "
                + TableGroup.GROUPPICTURE + ") VALUES(NULL,?,?)";
        Account founder = getAccountByUsername(group.getParticipants().get(0).getUserName());
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, group.getName());
            pstmt.setString(2, group.getGroupPicture());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                if (rs.getInt(1) > 0) {
                    group.setGroupID(rs.getInt(1));
                }
                System.out.println("Inserted into DB:\t" + group + founder.getProfile());
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("[SQLITE_CONSTRAINT]  Abort due to constraint violation (UNIQUE constraint failed: groupChats.groupName)")) {
                throw new Exception("GroupName is already in use!");
            }
            e.printStackTrace();
        }
        sql = "INSERT OR IGNORE INTO " + TableParticipants.NAME + " (" + TableParticipants.PID + ", " + TableParticipants.IDGROUP + ", "
                + TableParticipants.IDACCOUNT + ") VALUES(NULL,?,?)";
        for (Profile member : group.getParticipants()) {
            Account memberAcc = getAccountByUsername(member.getUserName());
            try (Connection conn = this.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, group.getGroupID());
                pstmt.setInt(2, memberAcc.getAid());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stored Messages Table
     */

    public void insertMessage(Message message, String userName) {
        Account receiverAcc = getAccountByUsername(userName);
        String sql = "INSERT INTO " + TableMessages.NAME + "(" + TableMessages.SMID + ", " + TableMessages.AID
                + ", " + TableMessages.TEXT
                + ", " + TableMessages.SENDER
                + ", " + TableMessages.RECEIVER
                + ", " + TableMessages.TIMESTAMP
                + ") VALUES(NULL,?,?,?,?,?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, receiverAcc.getAid());
            pstmt.setString(2, message.getText());
            pstmt.setString(3, message.getSender());
            pstmt.setString(4, message.getReceiver());
            pstmt.setString(5, Util.dateFormat.format(message.getTimestamp()));
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                System.out.println("Inserted into Messages DB of " + userName + ":\t" + message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Message> getStoredMessagesOfAccount(Account loggedAccount) {
        ArrayList<Message> messages = new ArrayList<>();
        String sql = "SELECT " + TableMessages.TEXT
                + "," + TableMessages.RECEIVER
                + "," + TableMessages.SENDER
                + "," + TableMessages.TIMESTAMP
                + " FROM " + TableMessages.NAME + " WHERE " + TableMessages.AID + " = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, loggedAccount.getAid());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Message msg = new Message(rs.getString(1), rs.getString(4), rs.getString(3),
                        rs.getString(2));
                messages.add(msg);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }

    public void removeStoredMessagesOfAcoount(Account loggedAccount) {
        String sql = "DELETE FROM " + TableMessages.NAME + " WHERE " + TableMessages.AID + " = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, loggedAccount.getAid());
            pstmt.execute();
        } catch (SQLException e) {
            //System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    //This method is for debugging.
    private void resetTable() {
        String sql = "DELETE FROM " + TableAccount.NAME + " WHERE " + TableAccount.AID + " = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= lastRow; i++) {
                pstmt.setInt(1, i);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public void dropTableAccount() {
        dropTable(TableAccount.NAME);
    }

    public void dropTableContactList() {
        dropTable(TableContact.NAME);
    }

    public void dropTableGroup() {
        dropTable(TableGroup.NAME);
    }

    public void dropTableParticipants() {
        dropTable(TableParticipants.NAME);
    }

    public void dropTableMessages() {
        dropTable(TableMessages.NAME);
    }

    public void dropTable(String tableName) {
        String sql = "DROP TABLE " + tableName + "";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        //createNewDatabase("sojaping.db");
        DatabaseService db = new DatabaseService(SOJAPING);
        db.dropTableContactList();
        db.dropTableAccount();
        db.dropTableParticipants();
        db.dropTableGroup();
        db.dropTableMessages();

        createNewTableAccount();
        createNewTableContactList();
        createNewTableGroup();
        createNewTableParticipants();
        createNewTableMessages();

        System.out.println("Insert");
        Account acc = new AccountBuilder().setUserName("phil").setPassword("phil")
                .setAboutMe("Hi, I'm using SOJAPING.")
                .setLanguages(Arrays.asList("de", "en"))
                .createAccount();
        Account acc2 = new AccountBuilder().setUserName("jan").setPassword("jan")
                .setLanguages(Arrays.asList("de", "en", "es", "hi"))
                .setAboutMe("Hi, I'm using SOJAPING.").createAccount();
        Account acc3 = new AccountBuilder().setUserName("irina").setPassword("irina")
                .setLanguages(Arrays.asList("de", "en", "ru"))
                .setAboutMe("Hi, I'm using SOJAPING.").createAccount();
        Account acc4 = new AccountBuilder().setUserName("sophie").setPassword("sophie")
                .setLanguages(Arrays.asList("de", "en", "fr"))
                .setAboutMe("Hi, I'm using SOJAPING.").createAccount();
        db.insertAccount(acc);
        db.insertAccount(acc2);
        db.insertAccount(acc3);
        db.insertAccount(acc4);
        Group group = new Group("#testGroup");
        group.addParticipant(acc4.getProfile());
        db.insertGroup(group);
        db.insertParticipant(group, acc.getProfile());
        db.insertParticipant(group, acc2.getProfile());
        db.insertParticipant(group, acc3.getProfile());
        System.out.println("in main: Group after insert: " + group);
        ArrayList<Group> myGroups = db.getMyGroups(acc4);
        System.out.println(myGroups);
        //db.selectAllAccounts();
        /*
        db.insertContactOfAccount(acc, acc2.getProfile());
        db.insertContactOfAccount(acc, acc3.getProfile());

        Account accTest=db.getAccountByLoginUser(new LoginUser("phil", "phil"));
        System.out.println("Contacts of " + accTest.getUserName());
        db.getAllContactsOfAccount(accTest).forEach(System.out::println);
        */
        //ArrayList<Profile> onlineUser = db.getOnlineAccounts();
    }

}
