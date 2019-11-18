package test.server;

import org.junit.Before;
import org.junit.Test;
import server.DatabaseService;

import java.sql.*;

import static org.junit.Assert.assertEquals;

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
                    result = rs.getString("TABLE_NAME");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        assertEquals("account", result);
    }






}
