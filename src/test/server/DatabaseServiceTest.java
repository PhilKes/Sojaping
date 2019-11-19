package test.server;

import common.data.Account;
import common.data.AccountBuilder;
import org.junit.Before;
import org.junit.Test;
import server.DatabaseService;

import java.sql.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DatabaseServiceTest {
    DatabaseService db;
    private static String SOJAPINGTEST = "sojapingTest.db";

    @Before
    public void createTestDatabase(){
        DatabaseService.createNewDatabase(SOJAPINGTEST);
        db = new DatabaseService(SOJAPINGTEST);
    }

    @Test
    public void testCreateNewTableAccount(){
        DatabaseService.createNewTableAccount();
        String result = "";
        try (Connection conn = DriverManager.getConnection(DatabaseService.URL)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet rs = meta.getTables(null, null, null, new String[]{"TABLE"});
                System.out.println("Printing TABLE_TYPE \"TABLE\" ");
                System.out.println("----------------------------------");
                while(rs.next()) {
                    System.out.println(rs.getString("TABLE_NAME"));
                    result += rs.getString("TABLE_NAME");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        assertTrue(result.contains("account"));
    }

    @Test
    public void testCreateNewTableContactList(){
        DatabaseService.createNewTableContactList();
        String result = "";
        try (Connection conn = DriverManager.getConnection(DatabaseService.URL)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet rs = meta.getTables(null, null, null, new String[]{"TABLE"});
                System.out.println("Printing TABLE_TYPE \"TABLE\" ");
                System.out.println("----------------------------------");
                while(rs.next()) {
                    System.out.println(rs.getString("TABLE_NAME"));
                    result += rs.getString("TABLE_NAME");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        assertTrue(result.contains("contactList"));
        //assertEquals("contactList", result);
    }

    @Test
    public void testInsertAccount(){
        DatabaseService.createNewTableAccount();
        Account acc = new AccountBuilder().setUserName("ddd").setPassword("aaa")
                .setAboutMe("This is the insert test.").createAccount();
        Account res = null;
        try {
            db.insertAccount(acc);

        } catch (Exception e) {
            e.printStackTrace();
        }
        String sql = "SELECT * FROM account WHERE userName = ?";
        try(Connection conn = DriverManager.getConnection(DatabaseService.URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, acc.getUserName());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                res = new AccountBuilder().setAid(rs.getInt("aid"))
                        .setUserName(rs.getString("userName"))
                        .setPassword(rs.getString("password"))
                        .setStatus(rs.getInt("status"))
                        .setAboutMe(rs.getString("aboutMe"))
                        .setProfilePicture(rs.getString("profilePicture"))
                        .createAccount();
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        System.out.println(acc.equals(res));
        assertTrue(acc.equals(res));
    }





}
