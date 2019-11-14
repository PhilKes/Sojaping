package server;


import client.LoginUser;
import common.data.Account;
import common.data.AccountBuilder;

import java.sql.*;
import java.sql.ResultSet;
import java.sql.Connection;

public class DatabaseService {
    private static final String STATUS="status";
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

    public static void createNewTable() {
        // SQLite connection string
        String url = "jdbc:sqlite:assets/sojaping.db";

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS account (\n"
                + "    aid integer PRIMARY KEY,\n"
                + "    userName text NOT NULL UNIQUE,\n"
                + "    password text NOT NULL,\n"
                + "    "+STATUS+" integer NOT NULL,\n"
                + "    aboutMe text,\n"
                + "    profilePicture text\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void selectAll(){
        String sql = "SELECT aid, userName, status, " +
                "aboutMe, profilePicture FROM account";
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getInt("aid") +  "\t" +
                        rs.getString("userName") + "\t" +
                        rs.getInt(STATUS) +  "\t" +
                        rs.getString("aboutMe") + "\t" +
                        rs.getString("profilePicture"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insert(Account acc) throws Exception {
        String sql = "INSERT INTO account(aid, userName, password, status, aboutMe, profilePicture) VALUES(NULL,?,?,?,?,?)";
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

    public void update(Account acc){
        String sql ="UPDATE account SET userName = ? , "
                + "password = ? , "
                + "status = ? , "
                + "aboutMe = ?, "
                + "profilePicture = ? "
                + "WHERE aid = ?";
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

    public void delete(Account acc){
        String sql= "DELETE FROM account WHERE aid = ?";

        try(Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, acc.getAid());
            pstmt.executeUpdate();
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    private void resetTable(){
        String sql = "DELETE FROM account WHERE aid = ?";
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

    private void dropTable(){
        String sql = "DROP TABLE account";
        try(Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.executeUpdate();
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }

    }

    public Account getAccountByLoginUser(LoginUser user){
        String sql = "SELECT * FROM account WHERE userName = ? AND password = ?";
        Account acc = null;
        try(Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            ResultSet rs= pstmt.executeQuery();
            while (rs.next()) {
                acc = new AccountBuilder().setAid(rs.getInt("aid")).setUserName(rs.getString("userName"))
                        .setPassword(rs.getString("password")).setStatus(rs.getInt(STATUS))
                        .setAboutMe(rs.getString("aboutMe")).setProfilePicture(rs.getString("profilePicture"))
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
        db.dropTable();
        createNewTable();

        System.out.println("Insert");
        Account account = new AccountBuilder().setUserName("Sophie").setPassword("abc").setAboutMe("I'm not happy.").createAccount();
        db.insert(account);
        db.selectAll();
        System.out.println(account.getAid());
        account.setUserName("Irina");
        db.update(account);
        System.out.println();
        db.selectAll();
        //db.delete(account);
        System.out.println();
        //db.selectAll();
        //db.resetTable();
        //db.selectAll();

    }


}
