package server;


import common.data.LoginUser;
import common.data.Account;
import common.data.AccountBuilder;
import common.data.Profile;

import java.sql.*;
import java.sql.ResultSet;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private static final String AID = "aid";
    private static final String USERNAME = "userName";
    private static final String PASSWORD = "password";
    private static final String STATUS = "status";
    private static final String ABOUTME = "aboutMe";
    private static final String PROFILEPICTURE = "profilePicture";
    private static final String LID = "lid";
    static int lastRow;

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

    private Connection connect(){
        String url = "jdbc:sqlite:assets/sojaping.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void createNewTableAccount() {
        // SQLite connection string
        String url = "jdbc:sqlite:assets/sojaping.db";

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS account (\n"
                +       AID+" integer PRIMARY KEY autoincrement,\n"
                +       USERNAME+" text NOT NULL UNIQUE,\n"
                +       PASSWORD+" text NOT NULL,\n"
                +       STATUS+" integer NOT NULL,\n"
                +       ABOUTME+" text,\n"
                +       PROFILEPICTURE+" text\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createNewTableContactList(){
        String url = "jdbc:sqlite:assets/sojaping.db";
        String sql = "CREATE TABLE IF NOT EXISTS contactList (\n"
                +       LID+" integer PRIMARY KEY,\n"
                +       AID+" integer NOT NULL,\n" //aid of the "owner" of this list
                +       USERNAME+" text NOT NULL,\n"
                +       STATUS+" integer NOT NULL,\n"
                +       ABOUTME+" text,\n"
                +       PROFILEPICTURE+" text,\n"
                +       "FOREIGN KEY ("+AID+")\n"
                +       "REFERENCES account("+AID+")\n"
                +       "ON DELETE CASCADE"
                +       ");";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void selectAllAccounts(){
        String sql = "SELECT "+AID+", "+USERNAME+", "+STATUS+", " +
                ABOUTME+", "+PROFILEPICTURE+" FROM account";
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getInt(AID) +  "\t" +
                        rs.getString(USERNAME) + "\t" +
                        rs.getInt(STATUS) +  "\t" +
                        rs.getString(ABOUTME) + "\t" +
                        rs.getString(PROFILEPICTURE));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void selectAllContactsOfAccount(Account acc){
        String sql = "SELECT * FROM contactList WHERE "+AID+" = ?";
        try(Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, acc.getAid());
            ResultSet rs= pstmt.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getInt(LID) + "\t"
                + rs.getInt(AID) + "\t"
                + rs.getString(USERNAME) + "\t"
                + rs.getInt(STATUS) + "\t"
                + rs.getString(ABOUTME) + "\t"
                + rs.getString(PROFILEPICTURE));
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Profile> getAllContactsOfAccount(Account acc){
        List<Profile> contacts = new ArrayList<>();
        String sql = "SELECT * FROM contactList WHERE "+AID+" = ?";
        try(Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, acc.getAid());
            ResultSet rs= pstmt.executeQuery();
            while (rs.next()) {
                contacts.add(new Profile(rs.getString(USERNAME), rs.getInt(STATUS),
                        rs.getString(ABOUTME), rs.getString(PROFILEPICTURE)));
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return contacts;
    }

    public void insertAccount(Account acc) throws Exception {
        String sql = "INSERT INTO account("+AID+", "+USERNAME+", "+PASSWORD+", "+STATUS+", "
                +ABOUTME+", "+PROFILEPICTURE+") VALUES(NULL,?,?,?,?,?)";
        try(Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            pstmt.setString(1, acc.getUserName());
            pstmt.setString(2, acc.getPassword());
            pstmt.setInt(3, acc.getStatus());
            pstmt.setString(4, acc.getAboutMe());
            pstmt.setString(5, acc.getProfilePicture());
            pstmt.executeUpdate();
            ResultSet rs=pstmt.getGeneratedKeys();
            if(rs.next()){
                acc.setAid(rs.getInt(1));
                lastRow = rs.getInt(1);
                System.out.println("Inserted into DB:\t"+acc);
            }
        }catch(SQLException e){
            if(e.getMessage().contains("[SQLITE_CONSTRAINT]  Abort due to constraint violation (UNIQUE constraint failed: account.userName)")){
                throw new Exception("Username is already in use!");
            }
            e.printStackTrace();
        }
    }

    public void insertContactOfAccount(Account account, Profile contact) throws Exception {
        String sql = "INSERT INTO contactList("+LID+", "+AID +", "+USERNAME+","+STATUS+", "
                +ABOUTME+", "+PROFILEPICTURE+") VALUES(NULL,?,?,?,?,?)";
        try(Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            pstmt.setInt(1, account.getAid());
            pstmt.setString(2, contact.getUserName());
            pstmt.setInt(3, contact.getStatus());
            pstmt.setString(4, contact.getAboutMe());
            pstmt.setString(5, contact.getProfilePicture());
            pstmt.executeUpdate();
            ResultSet rs=pstmt.getGeneratedKeys();
            if(rs.next()){
                System.out.println("Inserted into ContactList DB of Account "+account+":\t"+contact);
            }
        }catch(SQLException e){
//            if(e.getMessage().contains("[SQLITE_CONSTRAINT]  Abort due to constraint violation (UNIQUE constraint failed: account.userName)")){
//                throw new Exception("User is already in the ContactList!");
//            }
            e.printStackTrace();
        }
    }

    public void update(Account acc){
        String sql ="UPDATE account SET "+USERNAME+" = ? , "
                +   PASSWORD+" = ? , "
                +   STATUS+" = ? , "
                +   ABOUTME+" = ?, "
                +   PROFILEPICTURE+" = ? "
                + "WHERE "+AID+" = ?";
        try(Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, acc.getUserName());
            pstmt.setString(2, acc.getPassword());
            pstmt.setInt(3, acc.getStatus());
            pstmt.setString(4, acc.getAboutMe());
            pstmt.setString(5, acc.getProfilePicture());
            pstmt.setInt(6, acc.getAid());
            pstmt.executeUpdate();
        }catch(SQLException  e){
            System.out.println(e.getMessage());
        }
    }

    public void deleteAccount(Account acc){
        String sql= "DELETE FROM account WHERE "+AID+" = ?";

        try(Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, acc.getAid());
            pstmt.executeUpdate();
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    private void resetTable(){
        String sql = "DELETE FROM account WHERE "+AID+" = ?";
        try(Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            for(int i = 1 ; i <= lastRow; i++) {
                pstmt.setInt(1,i);
                pstmt.executeUpdate();
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }

    }

    private void dropTableAccount(){
        String sql = "DROP TABLE account";
        try(Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.executeUpdate();
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    private void dropTableContactList(){
        String sql = "DROP TABLE contactList";
        try(Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.executeUpdate();
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public Account getAccountByLoginUser(LoginUser user){
        String sql = "SELECT * FROM account WHERE "+USERNAME+" = ? AND "+PASSWORD+" = ?";
        Account acc = null;
        try(Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            ResultSet rs= pstmt.executeQuery();
            while (rs.next()) {
                acc = new AccountBuilder().setAid(rs.getInt(AID)).setUserName(rs.getString(USERNAME))
                        .setPassword(rs.getString(PASSWORD)).setStatus(rs.getInt(STATUS))
                        .setAboutMe(rs.getString(ABOUTME)).setProfilePicture(rs.getString(PROFILEPICTURE))
                        .createAccount();
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return acc;
    }

    public static void main(String[] args) throws Exception {
        //createNewDatabase("sojaping.db");
        DatabaseService db = new DatabaseService();
        //db.dropTableAccount();
        //db.dropTableContactList();
        createNewTableAccount();
        createNewTableContactList();

        System.out.println("Insert");
        Account acc = new AccountBuilder().setUserName("aaa").setPassword("abc")
                .setAboutMe("I'm not happy.").createAccount();
        Account acc2 = new AccountBuilder().setUserName("bbb").setPassword("aaa")
                .setAboutMe("Not nice.").createAccount();
        Account acc3 = new AccountBuilder().setUserName("ccc").setPassword("aaa")
                .setAboutMe("Not nice.").createAccount();
        db.insertAccount(acc);
        db.selectAllAccounts();
        System.out.println(acc.getAid());
        System.out.println();
        db.selectAllAccounts();
        //db.deleteAccount(acc);
        System.out.println();
        db.insertAccount(acc2);
        db.insertContactOfAccount(acc, acc2.getProfile());
        db.insertContactOfAccount(acc, acc3.getProfile());
        db.selectAllContactsOfAccount(acc);
    }


}
