package test.common;

import common.data.Account;
import common.data.AccountBuilder;
import common.data.LoginUser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static common.JsonHelper.*;
import static org.junit.Assert.assertEquals;

public class JsonHelperTest {

    @Test
    public void testParsePacketWithObject(){
        Account obj= new AccountBuilder()
                .setUserName("Test")
                .setPassword("1234")
                .setAboutMe("This is a test").createAccount();
        System.out.println("Original Object:\n\t"+obj);
        String json= getJsonFromPacket("login",obj);
        System.out.println("JSON generated:\n\t"+json);
        Account generatedObj= getPacketFromJson(json).getData();
        System.out.println("Generated Object:\n\t"+generatedObj);
        assertEquals(obj.toString(),generatedObj.toString());
    }

    @Test
    public void testParsePacketWithPrimitive() {
        double d=2.36265;
        System.out.println("Original Primitive:\n\t"+d);
        String json= getJsonFromPacket("primitiveTest",d);
        System.out.println("JSON generated:\n\t"+json);
        double generatedDouble= getPacketFromJson(json).getData();
        System.out.println("Generated Primitive:\n\t"+generatedDouble);
        assertEquals(d,generatedDouble,1e-6);
    }

    @Test
    public void testParsePacketWithList() {
        List<LoginUser> list=new ArrayList<>(Arrays.asList(
                new LoginUser("name", "passwd"),
                new LoginUser("abc", "abc")));
        System.out.println("Original List:\n\t"+list);
        String json= getJsonFromPacket("list",list);
        System.out.println("JSON generated:\n\t"+json);
        List<LoginUser> list2 =getPacketFromJson(json).getData();
        System.out.println("Generated List:\n\t"+list);
        for(int i=0; i<list.size(); i++) {
            LoginUser u1=list.get(i);
            LoginUser u2=list2.get(i);
            assertEquals(u1.toString(),u2.toString());
        }

        /** Empty List */
        List<Account> list3=new LinkedList<>();
        System.out.println("Original List:\n\t"+list3);
        json= getJsonFromPacket("list",list3);
        System.out.println("JSON generated:\n\t"+json);
        List<Account> list4 =getPacketFromJson(json).getData();
        System.out.println("Generated List:\n\t"+list4);
        for(int i=0; i<list3.size(); i++) {
            Account u1=list3.get(i);
            Account u2=list4.get(i);
            assertEquals(u1.toString(),u2.toString());
        }
    }
}
