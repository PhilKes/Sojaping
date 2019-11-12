package common;

import client.LoginUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import common.data.Account;

import java.io.IOException;
import java.util.Map;

public class JsonHelper {


	public static String getJsonOfObject(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = "";
		try {
			ObjectNode node;
			if(object instanceof String)
				node = mapper.createObjectNode().put("value",object.toString());
			else
				node= mapper.convertValue(object, ObjectNode.class);
			node.put("_class",object.getClass().getTypeName());
			jsonString = mapper.writeValueAsString(node);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonString;
	}

	public static <T> T convertJsonToObject(String jsonInString){
		ObjectMapper mapper = new ObjectMapper();
		Object object = null;
		try {
			// JSON string to Java object
			Map<String, String> map = mapper.readValue(jsonInString, Map.class);
			//TODO create ResponseEntity for any sent JSON WITH "_class"
			if(!map.containsKey("_class"))
				return null;
			Class type=Class.forName(map.get("_class"));
			/** Read primitive types (String,Integer,...)*/
			if(map.containsKey("value"))
				//object = mapper.readValue(map.get("value"), type);
				return (T) map.get("value");
			else
				object = mapper.readValue(jsonInString, type);
			return (T) object;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			//TODO COULD NOT CAST TO _class TYPE
			e.printStackTrace();
			return null;
		}
	}
}
