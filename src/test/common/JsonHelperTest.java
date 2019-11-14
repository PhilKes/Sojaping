package test.common;

import client.LoginUser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static common.JsonHelper.*;
import static org.junit.Assert.assertEquals;

public class JsonHelperTest {

    @Test
    public void testGeneratePacket(){
        LoginUser user=new LoginUser("name","passwd");
        System.out.println("Original Object:\n\t"+user);
        String json= getJsonFromPacket("login",user);
        System.out.println("JSON generated:\n\t"+json);
        LoginUser generatedUser= (LoginUser) getPacketFromJson(json).getData();
        System.out.println("Generated Object:\n\t"+generatedUser);
        assertEquals(user.toString(),generatedUser.toString());

        double d=2.0;
        System.out.println("Original Primitive:\n\t"+d);
        json= getJsonFromPacket("primitiveTest",d);
        System.out.println("JSON generated:\n\t"+json);
        double generatedDouble= (Double) getPacketFromJson(json).getData();
        System.out.println("Generated Primitive:\n\t"+generatedDouble);

        List<LoginUser> list=new ArrayList<>();
        list.addAll(Arrays.asList(new LoginUser("name","passwd"),new LoginUser("abc","abc")));
        json= getJsonFromPacket("list",list);
        List<LoginUser> list2 = (List<LoginUser>) getPacketFromJson(json).getData();
        for(int i=0; i<list.size(); i++) {
            LoginUser u1=list.get(i);
            LoginUser u2=list.get(i);
            assertEquals(u1.getUserName(),u2.getUserName());
            assertEquals(u1.getPassword(),u2.getPassword());
        }
    }
}
