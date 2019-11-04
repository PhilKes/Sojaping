package server;


import data.Account;

import java.sql.*;
import java.sql.ResultSet;
import java.util.Vector;

public class DatabaseService {
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

    public static void connect() {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:assets/sojaping.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void createNewTable() {
        // SQLite connection string
        String url = "jdbc:sqlite:assets/sojaping.db";

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS account (\n"
                + "    aid integer PRIMARY KEY,\n"
                + "    userName text NOT NULL,\n"
                + "    status integer NOT NULL,\n"
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
        String url = "jdbc:sqlite:assets/sojaping.db";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getInt("aid") +  "\t" +
                        rs.getString("userName") + "\t" +
                        rs.getInt("status") +  "\t" +
                        rs.getString("aboutMe") + "\t" +
                        rs.getString("profilePicture"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insert(Account acc){
        insert(acc.getUserName(), acc.getStatus(), acc.getAboutMe(), acc.getProfilePicture());
    }


    private void insert(String userName, int status, String aboutMe, String profilePicture){
        String sql = "INSERT INTO account(aid, userName, status, aboutMe, profilePicture) VALUES(NULL,?,?,?,?)";
        String url = "jdbc:sqlite:assets/sojaping.db";
        try(Connection conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, userName);
            pstmt.setInt(2, status);
            pstmt.setString(3, aboutMe);
            pstmt.setString(4, profilePicture);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        //createNewDatabase("sojaping.db");
        connect();
        createNewTable();
        DatabaseService db = new DatabaseService();
        System.out.println("Insert");
        Account account = new Account();
        account.setUserName("Sophie");
        account.setStatus(1);
        account.setAboutMe("I#m not happy.");
        db.insert(account);
        db.selectAll();

    }


}
