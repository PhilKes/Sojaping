package common;

import client.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.data.Account;

import java.io.IOException;

public class JsonHelper {

	public static String getJsonOfObject(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = "";

		try {
			jsonString = mapper.writeValueAsString(object);
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

		} catch (IOException e) {
			e.printStackTrace();
		}
		return account;
	}
}
