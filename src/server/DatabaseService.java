package server;


import common.Util;
import common.data.Account;
import common.data.AccountBuilder;
import common.data.LoginUser;
import common.data.Profile;

import java.sql.Connection;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseService {
    private static final String AID="aid";
    private static final String USERNAME="userName";
    private static final String PASSWORD="password";
    //private static final String STATUS="status";
    private static final String ABOUTME="aboutMe";
    private static final String PROFILEPICTURE="profilePicture";
    private static final String LID="lid";
    private static final String LANGUAGES="languages";
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
        String sql="CREATE TABLE IF NOT EXISTS account (\n"
                + AID + " integer PRIMARY KEY autoincrement,\n"
                + USERNAME + " text NOT NULL UNIQUE,\n"
                + PASSWORD + " text NOT NULL,\n"
                //+ STATUS + " integer NOT NULL,\n"
                + ABOUTME + " text,\n"
                + PROFILEPICTURE + " text,\n"
                + LANGUAGES + " text NOT NULL"
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
        String sql="CREATE TABLE IF NOT EXISTS contactList (\n"
                + LID + " integer PRIMARY KEY,\n"
                + AID + " integer NOT NULL,\n" //aid of the "owner" of this list
                + USERNAME + " text NOT NULL,\n"
                //+ STATUS + " integer NOT NULL,\n"
                + ABOUTME + " text,\n"
                + PROFILEPICTURE + " text,\n"
                + "FOREIGN KEY (" + AID + ")\n"
                + "REFERENCES account(" + AID + ")\n"
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

    //This method is for debugging.
    public void selectAllAccounts() {
        String sql="SELECT " + AID + ", " + USERNAME + ", " + PASSWORD + ", " +
                ABOUTME + ", " + PROFILEPICTURE + ", " + LANGUAGES + " FROM account";
        try(Connection conn=this.connect();
            Statement stmt=conn.createStatement();
            ResultSet rs=stmt.executeQuery(sql)) {

            // loop through the result set
            while(rs.next()) {
                Account acc=new AccountBuilder().setAid(rs.getInt(AID)).setUserName(rs.getString(USERNAME))
                        .setPassword(rs.getString(PASSWORD)).setStatus(0)
                        .setAboutMe(rs.getString(ABOUTME)).setProfilePicture(rs.getString(PROFILEPICTURE))
                        .setLanguages(Util.joinedStringToList(rs.getString(LANGUAGES)))
                        .createAccount();
                System.out.println(acc);
            }
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insertAccount(Account acc) throws Exception {
        String sql="INSERT INTO account(" + AID + ", " + USERNAME + ", " + PASSWORD + ", "
                + ABOUTME + ", " + PROFILEPICTURE + ", " + LANGUAGES + ") VALUES(NULL,?,?,?,?,?)";
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
        }
        catch(SQLException e) {
            if(e.getMessage().contains("[SQLITE_CONSTRAINT]  Abort due to constraint violation (UNIQUE constraint failed: account.userName)")) {
                throw new Exception("Username is already in use!");
            }
            e.printStackTrace();
        }
        String sql2="SELECT * FROM account WHERE userName = ?";
        try(Connection conn2=DriverManager.getConnection(DatabaseService.URL);
            PreparedStatement pstmt2=conn2.prepareStatement(sql2)) {
            pstmt2.setString(1, acc.getUserName());
            ResultSet rs=pstmt2.executeQuery();
            while(rs.next()) {
                acc.setAid(rs.getInt(AID));
            }
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(Account acc) {
        String sql="UPDATE account SET " + USERNAME + " = ? , "
                + PASSWORD + " = ? , "
                //+ STATUS + " = ? , "
                + ABOUTME + " = ?, "
                + PROFILEPICTURE + " = ? "
                + LANGUAGES + " = ? "
                + "WHERE " + AID + " = ?";
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql)) {
            pstmt.setString(1, acc.getUserName());
            pstmt.setString(2, acc.getPassword());
            pstmt.setString(3, acc.getAboutMe());
            pstmt.setString(4, acc.getProfilePicture());
            pstmt.setString(5, String.join(",", acc.getLanguages()));
            pstmt.setInt(6, acc.getAid());
            pstmt.executeUpdate();
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Account getAccountByLoginUser(LoginUser user) {
        String sql="SELECT * FROM account WHERE " + USERNAME + " = ? AND " + PASSWORD + " = ?";
        Account acc=null;
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            ResultSet rs=pstmt.executeQuery();
            while(rs.next()) {
                acc=new AccountBuilder().setAid(rs.getInt(AID)).setUserName(rs.getString(USERNAME))
                        .setPassword(rs.getString(PASSWORD)).setStatus(0)
                        .setAboutMe(rs.getString(ABOUTME)).setProfilePicture(rs.getString(PROFILEPICTURE))
                        .setLanguages(Util.joinedStringToList(rs.getString(LANGUAGES)))
                        .createAccount();
            }
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return acc;
    }

    public void deleteAccount(Account acc) {
        String sql="DELETE FROM account WHERE " + AID + " = ?";

        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql)) {
            pstmt.setInt(1, acc.getAid());
            pstmt.executeUpdate();
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //This method is for debugging.
    public void selectAllContactsOfAccount(Account acc) {
        String sql="SELECT * FROM contactList WHERE " + AID + " = ?";
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql)) {
            pstmt.setInt(1, acc.getAid());
            ResultSet rs=pstmt.executeQuery();
            while(rs.next()) {
                System.out.println(rs.getInt(LID) + "\t"
                        + rs.getInt(AID) + "\t"
                        + rs.getString(USERNAME) + "\t"
                        + rs.getString(ABOUTME) + "\t"
                        + rs.getString(PROFILEPICTURE));
            }
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public ArrayList<Profile> getAllContactsOfAccount(Account acc) {
        ArrayList<Profile> contacts=new ArrayList<>();
        String sql="SELECT * FROM contactList WHERE " + AID + " = ?";
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql)) {
            pstmt.setInt(1, acc.getAid());
            ResultSet rs=pstmt.executeQuery();
            while(rs.next()) {
                contacts.add(new Profile(rs.getString(USERNAME), 0,
                        rs.getString(ABOUTME), rs.getString(PROFILEPICTURE),
                        Util.joinedStringToList(rs.getString(LANGUAGES))));
            }
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return contacts;
    }

    public void insertContactOfAccount(Account account, Profile contact) {
        String sql="INSERT INTO contactList(" + LID + ", " + AID + ", " + USERNAME + ", "
                + LANGUAGES + ", " + ABOUTME + ", " + PROFILEPICTURE + ") VALUES(NULL,?,?,?,?,?)";
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, account.getAid());
            pstmt.setString(2, contact.getUserName());
            pstmt.setString(3, String.join(",", account.getLanguages()));
            pstmt.setString(4, contact.getAboutMe());
            pstmt.setString(5, contact.getProfilePicture());
            pstmt.executeUpdate();
            ResultSet rs=pstmt.getGeneratedKeys();
            if(rs.next()) {
                System.out.println("Inserted into ContactList DB of Account " + account + ":\t" + contact);
            }
        }
        catch(SQLException e) {
//            if(e.getMessage().contains("[SQLITE_CONSTRAINT]  Abort due to constraint violation (UNIQUE constraint failed: account.userName)")){
//                throw new Exception("User is already in the ContactList!");
//            }
            e.printStackTrace();
        }
        //TODO: Beim Aufruf prüfen, dass man sich nicht selbst oder jemanden, der schon enthalten ist, einfügt.
    }

    //This method is for debugging.
    private void resetTable() {
        String sql="DELETE FROM account WHERE " + AID + " = ?";
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql)) {
            for(int i=1; i<=lastRow; i++) {
                pstmt.setInt(1, i);
                pstmt.executeUpdate();
            }
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public void dropTableAccount() {
        String sql="DROP TABLE account";
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void dropTableContactList() {
        String sql="DROP TABLE contactList";
        try(Connection conn=this.connect();
            PreparedStatement pstmt=conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /*    public ArrayList<Profile> getOnlineAccounts() {
            ArrayList<Profile> onlineAccounts=new ArrayList<>();
            String sql="SELECT " + USERNAME + ", " + STATUS + ", " +
                    ABOUTME + ", " + PROFILEPICTURE + " FROM account WHERE " + STATUS + " = 1";
            try(Connection conn=this.connect();
                Statement stmt=conn.createStatement();
                ResultSet rs=stmt.executeQuery(sql)) {
                while(rs.next()) {
                    onlineAccounts.add(new Profile(rs.getString(USERNAME), rs.getInt(STATUS),
                            rs.getString(ABOUTME), rs.getString(PROFILEPICTURE),

                            Arrays.asList("de","en")));
                    System.out.println(rs.getString(USERNAME) + "\t" +
                            rs.getInt(STATUS) + "\t" +
                            rs.getString(ABOUTME) + "\t" +
                            rs.getString(PROFILEPICTURE));
                }
            }
            catch(SQLException e) {
                System.out.println(e.getMessage());
            }
            return onlineAccounts;
        }*/
    public static void main(String[] args) throws Exception {
        //createNewDatabase("sojaping.db");
        DatabaseService db=new DatabaseService(SOJAPING);
        /*db.dropTableContactList();
        db.dropTableAccount();

        createNewTableAccount();
        createNewTableContactList();

        System.out.println("Insert");
        Account acc=new AccountBuilder().setUserName("phil").setPassword("phil")
                .setAboutMe("Hi, I'm using SOJAPING.")
                .setLanguages(Arrays.asList("de","en"))
                .createAccount();
        Account acc2=new AccountBuilder().setUserName("jan").setPassword("jan")
                .setLanguages(Arrays.asList("de","en","es"))
                .setAboutMe("Hi, I'm using SOJAPING.").createAccount();
        Account acc3=new AccountBuilder().setUserName("irina").setPassword("irina")
                .setLanguages(Arrays.asList("de","en","ru"))
                .setAboutMe("Hi, I'm using SOJAPING.").createAccount();
        Account acc4=new AccountBuilder().setUserName("sophie").setPassword("sophie")
                .setLanguages(Arrays.asList("de","en","fr"))
                .setAboutMe("Hi, I'm using SOJAPING.").createAccount();
        db.insertAccount(acc);
        //db.selectAllAccounts();
        //System.out.println(acc.getAid());
        //System.out.println();
        //db.selectAllAccounts();
        //db.deleteAccount(acc);
        //System.out.println();
        db.insertAccount(acc2);
        db.insertAccount(acc3);
        db.insertAccount(acc4);*/
        db.selectAllAccounts();
        //db.insertContactOfAccount(acc, acc2.getProfile());
        //db.insertContactOfAccount(acc, acc3.getProfile());
        //db.selectAllContactsOfAccount(acc);
        //ArrayList<Profile> onlineUser = db.getOnlineAccounts();
    }


}
