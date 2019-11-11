package common;

import client.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.data.Account;

import java.io.IOException;

public class JsonHelper {

	public static String getJsonOfObject (Object object){

		ObjectMapper mapper = new ObjectMapper();
		String jsonString = "";

		try {

			// Java objects to JSON string - compact-print
			jsonString = mapper.writeValueAsString(object);

			System.out.println(jsonString);

			// Java objects to JSON string - pretty-print
			String jsonInString2 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);

			System.out.println(jsonInString2);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return jsonString;
	}

	public static Account convertJsonToAccount(String jsonInString) {
		ObjectMapper mapper = new ObjectMapper();
		Account account = null;
		try {
			// JSON string to Java object
			account = mapper.readValue(jsonInString, Account.class);

			// compact print
			System.out.println("Received Json from client: " + account);
			// pretty print
			String prettyStaff1 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(account);
			System.out.println(prettyStaff1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return account;
	}

	public static LoginUser convertJsonToLoginUser(String jsonInString) {
		ObjectMapper mapper = new ObjectMapper();
		LoginUser account = null;
		try {
			// JSON string to Java object
			account = mapper.readValue(jsonInString, LoginUser.class);

			// compact print
			System.out.println("Received Json from client: " + account);
			// pretty print
			String prettyStaff1 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(account);
			System.out.println(prettyStaff1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return account;
	}
}
