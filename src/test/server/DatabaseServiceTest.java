package test.server;

import common.Util;
import common.data.Account;
import common.data.AccountBuilder;
import common.data.LoginUser;
import common.data.Profile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import server.DatabaseService;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DatabaseServiceTest {
    DatabaseService db;
    private static String SOJAPINGTEST = "sojapingTest.db";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

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
        //First create the table account, because the table contactList references this table.
        DatabaseService.createNewTableAccount();
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
        assertTrue(result.contains(DatabaseService.TableContact.NAME));
    }

    @Test
    public void testDropTableAccount(){
        db.dropTableAccount();
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
        assertFalse(result.contains("account"));
    }

    @Test
    public void testDropTableContactList(){
        db.dropTableContactList();
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
        assertFalse(result.contains(DatabaseService.TableContact.NAME));
    }

    @Test
    public void testInsertAccount(){
        //Delete existing table to ensure that the username is free.
        db.dropTableAccount();
        DatabaseService.createNewTableAccount();
        Account acc = new AccountBuilder().setUserName("aaa").setPassword("aaa")
                .setAboutMe("This is the insert test.")
                .setLanguages(Arrays.asList("de", "en", "ru")).createAccount();
        Account res = null;
        try {
            db.insertAccount(acc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String sql="SELECT * FROM " + DatabaseService.TableAccount.NAME + " WHERE " + DatabaseService.TableAccount.AID + " = ?";
        try(Connection conn = DriverManager.getConnection(DatabaseService.URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, acc.getAid());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                res=new AccountBuilder().setAid(rs.getInt(DatabaseService.TableAccount.AID))
                        .setUserName(rs.getString(DatabaseService.TableAccount.USERNAME))
                        .setPassword(rs.getString(DatabaseService.TableAccount.PASSWORD))
                        .setStatus(0)
                        .setAboutMe(rs.getString(DatabaseService.TableAccount.ABOUTME))
                        .setProfilePicture(rs.getString(DatabaseService.TableAccount.PROFILEPICTURE))
                        .setLanguages(Util.joinedStringToList(rs.getString(DatabaseService.TableAccount.LANGUAGES)))
                        .createAccount();
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        assertTrue(acc.equals(res));
    }

    @Test
    public void testInsertExistingAccount() throws Exception {
        //Delete existing table to ensure that the username is free.
        db.dropTableAccount();
        DatabaseService.createNewTableAccount();
        Account acc = new AccountBuilder().setUserName("ggg").setPassword("aaa")
                .setAboutMe("This is the insert test with an existing user.").createAccount();
        //Insert the account first -> no problem.
        db.insertAccount(acc);
        exceptionRule.expect(Exception.class);
        exceptionRule.expectMessage("Username is already in use!");
        //Try to insert the same account twice -> an error occurs.
        db.insertAccount(acc);
    }

    @Test
    public void testUpdate(){
        //Delete existing table to ensure that the username is free.
        db.dropTableAccount();
        DatabaseService.createNewTableAccount();
        Account acc = new AccountBuilder().setUserName("aaa").setPassword("aaa")
                .setAboutMe("This is the update test.").createAccount();
        try {
            db.insertAccount(acc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Set the new password and update the changes to the database.
        acc.setPassword("bbb");
        db.updateAccount(acc);
        Account res=db.getAccountById(acc.getAid());
                /*null;
        String sql = "SELECT * FROM "+ DatabaseService.TableAccount.NAME +" WHERE "+ DatabaseService.TableAccount.AID +" = ?";
        try(Connection conn = DriverManager.getConnection(DatabaseService.URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, acc.getAid());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                res = new AccountBuilder().setAid(rs.getInt(DatabaseService.TableAccount.AID))
                        .setUserName(rs.getString(DatabaseService.TableAccount.USERNAME))
                        .setPassword(rs.getString(DatabaseService.TableAccount.PASSWORD))
                        .setStatus(0)
                        .setAboutMe(rs.getString(DatabaseService.TableAccount.ABOUTME))
                        .setProfilePicture(rs.getString(DatabaseService.TableAccount.PROFILEPICTURE))
                        .createAccount();
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }*/
        assertTrue(acc.equals(res));
    }

    @Test
    public void testDeleteAccount(){
        //Delete existing table to ensure that the username is free.
        db.dropTableAccount();
        DatabaseService.createNewTableAccount();
        Account acc = new AccountBuilder().setUserName("aaa").setPassword("aaa")
                .setAboutMe("This is the delete test.").createAccount();
        try {
            db.insertAccount(acc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.deleteAccount(acc);
        Account res = null;
        String sql = "SELECT * FROM account WHERE aid = ?";
        try(Connection conn = DriverManager.getConnection(DatabaseService.URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, acc.getAid());
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
        assertEquals(null, res);
    }

    @Test
    public void testInsertContactOfAccount(){
        //Delete existing table to ensure that the username is free.
        db.dropTableAccount();
        db.dropTableContactList();
        DatabaseService.createNewTableAccount();
        DatabaseService.createNewTableContactList();
        Account acc = new AccountBuilder().setUserName("aaa").setPassword("aaa")
                .setAboutMe("This is the insert a contact test.").createAccount();
        Account friend = new AccountBuilder().setUserName("bbb").setPassword("aaa")
                .setAboutMe("I am the friend.").createAccount();
        try {
            db.insertAccount(acc);
            db.insertAccount(friend);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            db.insertContactOfAccount(acc, friend.getProfile());
        }
        catch(Exception e) {
            fail(e.getMessage());
        }
        List<Profile> contacts=db.getAllContactsOfAccount(acc);
        assertTrue(contacts.size()==1);
        assertTrue(friend.getProfile().equals(contacts.get(0)));
    }

    @Test
    public void testGetAllContactsOfAccount(){
        db.dropTableAccount();
        db.dropTableContactList();
        DatabaseService.createNewTableAccount();
        DatabaseService.createNewTableContactList();
        Account acc = new AccountBuilder().setUserName("aaa").setPassword("aaa")
                .setAboutMe("This is the get all contacts test.")
                .setLanguages(Arrays.asList("de", "en")).createAccount();
        Account friend = new AccountBuilder().setUserName("bbb").setPassword("aaa")
                .setAboutMe("I am the friend.")
                .setLanguages(Arrays.asList("fr", "ru")).createAccount();
        try {
            db.insertAccount(acc);
            db.insertAccount(friend);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            db.insertContactOfAccount(acc, friend.getProfile());
        }
        catch(Exception e) {
            fail(e.getMessage());
        }
        ArrayList<Profile> contacts = db.getAllContactsOfAccount(acc);
        boolean result = false;
        Profile contact = contacts.get(0);
        if(friend.getUserName().equals(contact.getUserName())
            && friend.getStatus() == contact.getStatus()
            && friend.getAboutMe().equals(contact.getAboutMe())
            &&((friend.getProfilePictureAsBase64() == null && contact.getProfilePictureAsBase64() == null)
            || friend.getProfilePictureAsBase64().equals(contact.getProfilePictureAsBase64()))){
            result = true;
        }
        assertTrue(result);
    }

    @Test
    public void testGetAccountByLoginUser(){
        //Delete existing table to ensure that the username is free.
        db.dropTableAccount();
        DatabaseService.createNewTableAccount();
        Account acc = new AccountBuilder().setUserName("aaa").setPassword("aaa")
                .setAboutMe("This is the get account by loginUser test.")
                .setLanguages(Arrays.asList("en"))
                .createAccount();
        try {
            db.insertAccount(acc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LoginUser user = new LoginUser(acc.getUserName(), acc.getPassword());
        Account result = db.getAccountByLoginUser(user);
        assertTrue(acc.equals(result));
    }
}
