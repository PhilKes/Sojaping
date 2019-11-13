package test.common;

import client.LoginUser;
import org.junit.Test;

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

        String json2= getJsonFromPacket("primitiveTest",2.0);
        Double generatedDouble= (Double) getPacketFromJson(json2).getData();
        System.out.println(json2);
        System.out.println(generatedDouble);
    }
}
