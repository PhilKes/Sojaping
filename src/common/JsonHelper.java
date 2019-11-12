package common;

import client.LoginUser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import common.data.Account;

import java.io.IOException;
import java.util.Map;

public class JsonHelper {

	public static String convertObjectToJson(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String jsonString = "";
		try {
			ObjectNode node;
			/** Wrap primitive types into value field*/
			if(isPrimitiveOrWrapper(object.getClass()))
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
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Object object = null;
		try {
			/** Get alls Key-Value pairs of JSON String*/
			Map<String, String> map = mapper.readValue(jsonInString, Map.class);
			if(!map.containsKey("_class"))
				return null;
			Class type=Class.forName(map.get("_class"));
			/** Read primitive types (String,Integer,...)*/
			if(map.containsKey("value"))
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

	public static boolean isPrimitiveOrWrapper(Class<?> type) {
		return (type == Double.class || type == Float.class || type == Long.class ||
				type == Integer.class || type == Short.class || type == Character.class ||
				type == Byte.class || type == Boolean.class || type == String.class);
	}
}
