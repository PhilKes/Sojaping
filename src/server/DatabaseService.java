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
        public static final String NAME = "account";
        public static final String AID = "aid";
        public static final String USERNAME = "userName";
        public static final String PASSWORD = "password";
        public static final String ABOUTME = "aboutMe";
        public static final String PROFILEPICTURE = "profilePicture";
        public static final String LANGUAGES = "languages";
    }

    public static class TableContact {
        public static final String NAME = "contact";
        public static final String CID = "cid";
        public static final String AIDO = "aido";
        public static final String AIDC = "aidc";
        public static final String BLOCKED = "blocked";
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
        public static final String ADMIN="admin";
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

    private static final String SOJAPING = "sojaping.db";
    public static String URL = "";
    static int lastRow;

    public DatabaseService(String url) {
        URL = "jdbc:sqlite:assets/" + url;
    }

    public static void createNewDatabase(String fileName) {
        String url = "jdbc:sqlite:assets/" + fileName;
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
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
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createNewTableContactList() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TableContact.NAME + " (\n"
                + TableContact.CID + " integer PRIMARY KEY,\n"
                + TableContact.AIDO + " integer NOT NULL,\n" //aid of the "owner" of this list
                + TableContact.AIDC + " integer NOT NULL,\n"
                + TableContact.BLOCKED + " boolean NOT NULL,\n"
                + "UNIQUE(" + TableContact.AIDO + "," + TableContact.AIDC + ")\n"
                + "FOREIGN KEY (" + TableContact.AIDO + ")\n"
                + "REFERENCES account(" + TableAccount.AID + ")\n"
                + "FOREIGN KEY (" + TableContact.AIDC + ")\n"
                + "REFERENCES account(" + TableAccount.AID + ")\n"
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

    public static void createNewTableGroup() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TableGroup.NAME + " (\n"
                + TableGroup.GID + " integer PRIMARY KEY,\n"
                + TableGroup.GROUPNAME + " text NOT NULL UNIQUE,\n"
                + TableGroup.GROUPPICTURE + " text);";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public static void createNewTableParticipants() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TableParticipants.NAME + " (\n"
                + TableParticipants.PID + " integer PRIMARY KEY,\n"
                + TableParticipants.IDGROUP + " integer NOT NULL,\n"
                + TableParticipants.IDACCOUNT + " integer NOT NULL,\n"
                + TableParticipants.ADMIN + " boolean NOT NULL,\n"
                + "UNIQUE(" + TableParticipants.IDGROUP + "," + TableParticipants.IDACCOUNT + ")\n"
                + "FOREIGN KEY (" + TableParticipants.IDGROUP + ")\n"
                + "REFERENCES " + TableGroup.NAME + "(" + TableGroup.GID + ")\n"
                + "FOREIGN KEY (" + TableParticipants.IDACCOUNT + ")\n"
                + "REFERENCES account(" + TableAccount.AID + ")\n"
                + "ON DELETE CASCADE"
                + ");";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
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
        String sql = "SELECT * FROM account";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // loop through the result set
            while (rs.next()) {
                Account acc = new AccountBuilder().setAid(rs.getInt(TableAccount.AID)).setUserName(rs.getString(TableAccount.USERNAME))
                        .setPassword(rs.getString(TableAccount.PASSWORD)).setStatus(0)
                        .setAboutMe(rs.getString(TableAccount.ABOUTME)).setProfilePicture(rs.getString(TableAccount.PROFILEPICTURE))
                        .setLanguages(Util.joinedStringToList(rs.getString(TableAccount.LANGUAGES)))
                        .createAccount();
                System.out.println(acc);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Account Table
     */

    public void insertAccount(Account acc) throws Util.PacketException {
        String sql = "INSERT INTO " + TableAccount.NAME + "(" + TableAccount.AID + ", " + TableAccount.USERNAME + ", " + TableAccount.PASSWORD + ", "
                + TableAccount.ABOUTME + ", " + TableAccount.PROFILEPICTURE + ", " + TableAccount.LANGUAGES + ") VALUES(NULL,?,?,?,?,?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, acc.getUserName());
            pstmt.setString(2, acc.getPassword());
            //pstmt.setInt(3, acc.getStatus());
            pstmt.setString(3, acc.getAboutMe());
            pstmt.setString(4, acc.getProfilePicture());
            pstmt.setString(5, String.join(",", acc.getLanguages()));
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                acc.setAid(rs.getInt(1));
                lastRow = rs.getInt(1);
                System.out.println("Inserted into DB:\t" + acc);
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("[SQLITE_CONSTRAINT]  Abort due to constraint violation (UNIQUE constraint failed: account.userName)")) {
                throw new Util.PacketException("Username is already in use!");
            }
            e.printStackTrace();
        }
        String sql2 = "SELECT * FROM " + TableAccount.NAME + " WHERE userName = ?";
        try (Connection conn2 = DriverManager.getConnection(DatabaseService.URL);
             PreparedStatement pstmt2 = conn2.prepareStatement(sql2)) {
            pstmt2.setString(1, acc.getUserName());
            ResultSet rs = pstmt2.executeQuery();
            while (rs.next()) {
                acc.setAid(rs.getInt(TableAccount.AID));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateAccount(Account acc) {
        String sql = "UPDATE " + TableAccount.NAME + " SET " + TableAccount.USERNAME + " = ? , "
                + TableAccount.PASSWORD + " = ? , "
                + TableAccount.ABOUTME + " = ?, "
                + TableAccount.PROFILEPICTURE + " = ?, "
                + TableAccount.LANGUAGES + " = ? "
                + "WHERE " + TableAccount.AID + " = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, acc.getUserName());
            pstmt.setString(2, acc.getPassword());
            pstmt.setString(3, acc.getAboutMe());
            pstmt.setString(4, acc.getProfilePicture());
            pstmt.setString(5, String.join(",", acc.getLanguages()));
            pstmt.setInt(6, acc.getAid());
            pstmt.executeUpdate();
            /** Debug */
            System.out.println("Updated: " + getAccountById(acc.getAid()));
        } catch (SQLException e) {
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
        StringBuilder builder = new StringBuilder("SELECT * FROM ").append(TableAccount.NAME);
        if (pars.size() > 0) {
            builder.append(" WHERE ");
        }
        builder.append(String.join(" AND ", pars.stream().map(p -> p.getKey() + " = ?").collect(Collectors.toList())));
        String sql = builder.toString();
        Account acc = null;
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < pars.size(); i++) {
                pstmt.setObject(i + 1, pars.get(i).getValue());
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                acc = new AccountBuilder().setAid(rs.getInt(TableAccount.AID)).setUserName(rs.getString(TableAccount.USERNAME))
                        .setPassword(rs.getString(TableAccount.PASSWORD)).setStatus(0)
                        .setAboutMe(rs.getString(TableAccount.ABOUTME)).setProfilePicture(rs.getString(TableAccount.PROFILEPICTURE))
                        .setLanguages(Util.joinedStringToList(rs.getString(TableAccount.LANGUAGES)))
                        .createAccount();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return acc;
    }

    public Profile getProfileByAccountId(int aid) {
        return getAccountById(aid).getProfile();
    }

    public void deleteAccount(Account acc) {
        String sql = "DELETE FROM " + TableAccount.NAME + " WHERE " + TableAccount.AID + " = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, acc.getAid());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Contact Table
     */
    //This method is for debugging.
    public void selectAllContactsOfAccount(Account acc) {
        String sql = "SELECT * FROM contact WHERE " + TableContact.AIDO + " = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, acc.getAid());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getInt(TableContact.CID) + "\t"
                        + rs.getInt(TableContact.AIDO) + "\t"
                        + rs.getInt(TableContact.AIDC) + "\t");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public ArrayList<Profile> getAllContactsOfAccount(Account acc) {
        ArrayList<Profile> profiles = new ArrayList<>();
        String sql = "SELECT " + TableContact.AIDC + "," + TableContact.BLOCKED + " FROM " + TableContact.NAME + " WHERE " + TableContact.AIDO + " = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, acc.getAid());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // contacts.add(new Contact(rs.getInt(TableContact.CID), rs.getInt(TableContact.AIDO), rs.getInt(TableContact.AIDC)));
                //if (!rs.getBoolean(TableContact.BLOCKED)) {
                Profile profile = getProfileByAccountId(rs.getInt(TableContact.AIDC));
                profile.setBlocked(rs.getBoolean(TableContact.BLOCKED));
                profiles.add(profile);
                //}
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return profiles;
    }

    public void insertContactOfAccount(Account account, Profile contact, boolean block) throws Util.PacketException {
        String sql = "INSERT OR REPLACE INTO " + TableContact.NAME + "(" + TableContact.CID + ", " + TableContact.AIDO + ", "
                + TableContact.AIDC + ", "
                + TableContact.BLOCKED + ") VALUES(NULL,?,?,?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, account.getAid());
            pstmt.setInt(2, getAccountByUsername(contact.getUserName()).getAid());
            pstmt.setBoolean(3, block);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                System.out.println("Inserted into ContactList DB of Account " + account + ":\t" + contact);
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("[SQLITE_CONSTRAINT]  Abort due to constraint violation (UNIQUE constraint failed: contact.aido, contact.aidc)")) {
                throw new Util.PacketException(contact.getUserName() + " is already in your contacts!");
            }
            //e.printStackTrace();
        }
    }

    public void removeContactOfAccount(Account account, Profile contact) throws Util.PacketException {
        String sql="DELETE FROM " + TableContact.NAME + " WHERE " + TableContact.AIDO + " = ? AND " + TableContact.AIDC + " = ?";
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, account.getAid());
            pstmt.setInt(2, getAccountByUsername(contact.getUserName()).getAid());
            pstmt.executeUpdate();
            pstmt.execute();
        }
        catch(SQLException e) {
            throw new Util.PacketException(e.getMessage());
            //e.printStackTrace();
        }
    }

    public boolean hasBlocked(String receiver, Account contact) {
        String sql = "SELECT " + TableContact.BLOCKED + " FROM " + TableContact.NAME + " WHERE " + TableContact.AIDO + " = ? AND "
                + TableContact.AIDC + " = ?";
        Account receiverAcc = getAccountByUsername(receiver);
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, receiverAcc.getAid());
            pstmt.setInt(2, contact.getAid());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Group Participant Table
     */
    public void insertParticipant(Group group, Group.Participant participant) {
        int particpantID = this.getAccountByUsername(participant.getUserName()).getAid();
        String sql = "INSERT INTO " + TableParticipants.NAME + " (" + TableParticipants.PID + ", " + TableParticipants.IDGROUP + ", "
                + TableParticipants.IDACCOUNT + ", "
                + TableParticipants.ADMIN + ") VALUES(NULL,?,?,?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, group.getGroupID());
            pstmt.setInt(2, particpantID);
            pstmt.setBoolean(3, participant.isAdmin());
            pstmt.executeUpdate();
            group.addParticipant(participant);
            System.out.println("Group after insert: " + group);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Group.Participant> getParticipants(Group group) {
        ArrayList<Group.Participant> participants=new ArrayList<>();
        String sql="SELECT * FROM " + TableParticipants.NAME + " WHERE " + TableParticipants.IDGROUP + " = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, group.getGroupID());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Group.Participant participant=new Group.Participant(getProfileByAccountId(rs.getInt(TableParticipants.IDACCOUNT)));
                boolean isAdmin=rs.getBoolean(TableParticipants.ADMIN);
                participant.setAdmin(isAdmin);
                participants.add(participant);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return participants;
    }

    public ArrayList<Group.Participant> getParticipants(String groupName) {
        ArrayList<Group.Participant> participants=new ArrayList<>();
        String sql = "SELECT " + TableGroup.GID + " FROM " + TableGroup.NAME + " WHERE " + TableGroup.GROUPNAME + " = ?";
        int gid = -1;
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                gid = rs.getInt(TableGroup.GID);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        String sql2="SELECT " + TableParticipants.IDACCOUNT + "," + TableParticipants.ADMIN + " FROM " + TableParticipants.NAME + " WHERE " + TableParticipants.IDGROUP + " = ?";
        try (Connection conn2 = this.connect();
             PreparedStatement pstmt2 = conn2.prepareStatement(sql2)) {
            pstmt2.setInt(1, gid);
            ResultSet rs2 = pstmt2.executeQuery();
            while (rs2.next()) {
                Group.Participant participant=new Group.Participant(getProfileByAccountId(rs2.getInt(TableParticipants.IDACCOUNT)));
                participant.setAdmin(rs2.getBoolean(TableParticipants.ADMIN));
                participants.add(participant);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return participants;
    }

    /**
     * Group Table
     */

    public ArrayList<Group> getMyGroups(Account acc) {
        ArrayList<Group> groups = new ArrayList<>();
        String sql="SELECT " + TableParticipants.IDGROUP + "," + TableParticipants.ADMIN + " FROM " + TableParticipants.NAME + " WHERE " +
                TableParticipants.IDACCOUNT + " = ?";
        String sql2="SELECT " + TableGroup.GROUPNAME + "," + TableGroup.GROUPPICTURE + " FROM " + TableGroup.NAME + " WHERE " +
                TableGroup.GID + " = ?";
        List<Integer> groupIDs = new ArrayList<>();
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, acc.getAid());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                groupIDs.add(rs.getInt(TableParticipants.IDGROUP));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        for (int i : groupIDs) {
            try (Connection conn2 = this.connect();
                 PreparedStatement pstmt2 = conn2.prepareStatement(sql2)) {
                pstmt2.setInt(1, i);
                ResultSet rs2 = pstmt2.executeQuery();
                while (rs2.next()) {
                    Group group = new Group(rs2.getString(TableGroup.GROUPNAME));
                    String ava=rs2.getString(TableGroup.GROUPPICTURE);
                    group.setGroupPicture(ava);
                    group.setGroupID(i);
                    //group.getParticipants().addAll(this.getParticipants(group));
                    groups.add(group);

                }
            } catch (SQLException f) {
                System.out.println(f.getMessage());
                f.printStackTrace();
            }
        }
        for (Group g : groups) {
            g.getParticipants().addAll(this.getParticipants(g));
        }

        return groups;
    }

    public void insertGroup(Group group) throws Exception {
        String sql="INSERT OR REPLACE INTO " + TableGroup.NAME + " (" + TableGroup.GID + ", " + TableGroup.GROUPNAME + ", "
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
                + TableParticipants.IDACCOUNT + ", "
                + TableParticipants.ADMIN + ") VALUES(NULL,?,?,?)";
        for(Group.Participant member : group.getParticipants()) {
            Account memberAcc = getAccountByUsername(member.getUserName());
            try (Connection conn = this.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, group.getGroupID());
                pstmt.setInt(2, memberAcc.getAid());
                pstmt.setBoolean(3, member.isAdmin());
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
        group.setGroupPicture("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");
        Group.Participant p1=new Group.Participant(acc.getProfile());
        p1.setAdmin(true);
        group.addParticipant(p1);
        db.insertGroup(group);
        Group.Participant p=new Group.Participant(acc4.getProfile());
        db.insertParticipant(group, p);
        Group.Participant p2=new Group.Participant(acc2.getProfile());
        db.insertParticipant(group, p2);
        Group.Participant p3=new Group.Participant(acc3.getProfile());
        db.insertParticipant(group, p3);
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
