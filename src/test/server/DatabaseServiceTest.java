package test.server;

import org.junit.Before;
import org.junit.Test;
import server.DatabaseService;

public class DatabaseServiceTest {
    DatabaseService db = new DatabaseService();
    private static String SOJAPINGTEST = "sojapingTest.db";
    @Before
    public void createTestDatabase(){
        DatabaseService.createNewDatabase(SOJAPINGTEST);
        DatabaseService.URL += SOJAPINGTEST;
    }






}
