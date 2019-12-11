package common;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import common.data.Packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static common.Constants.Json.*;

/**
 * Helper class providing methods to generate/parse Packets as Objects
 */
public class JsonHelper {

    public static final ObjectMapper mapper=new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Returns Packet Object as JSON String
     */
    public static String getJsonFromPacket(Packet packet) {
        String jsonString="";
        Object data=packet.getData();
        String context=packet.getContext();
        ObjectNode jsonPacket=mapper.createObjectNode();
        jsonPacket.put(METHOD_FIELD, context);
        try {
            ObjectNode node;
            node=mapper.createObjectNode();
            /** Wrap primitive types into value field*/
            if(data!=null) {
                /** Lists */
                if(data instanceof List<?>) {
                    List<?> list=(List<?>) data;
                    ArrayNode array=mapper.valueToTree(list);
                    if(!list.isEmpty()) {
                        String typeName=list.get(0).getClass().getTypeName();
                        node.put(CLASS_FIELD, LIST_CLASS + SEPERATOR + typeName);
                        for(JsonNode item : array)
                            ((ObjectNode) item).put(CLASS_FIELD, typeName);
                    }
                    else {
                        node.put(CLASS_FIELD, LIST_CLASS + SEPERATOR);
                    }
                    node.set(DATA_FIELD, array);
                }
                else {
                    /** Primitive or Common objects*/
                    if(isPrimitiveOrWrapper(data.getClass())) {
                        node=mapper.createObjectNode().put(PRIMITIVE_FIELD, data.toString());
                    }
                    else {
                        node=mapper.convertValue(data, ObjectNode.class);
                    }
                    node.put(CLASS_FIELD, data.getClass().getTypeName());
                }
            }
            jsonPacket.set(DATA_FIELD, node);
            jsonString=mapper.writeValueAsString(jsonPacket);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    public static String getJsonFromPacket(String context, Object data) {
        return getJsonFromPacket(new Packet(context, data));
    }

    /**
     * Returns Packet object from JSON String
     */
    public static Packet getPacketFromJson(String json) {
        JsonNode node=null;
        try {
            node=mapper.readTree(json);
            String method=node.get(METHOD_FIELD).asText();
            JsonNode data=node.get(DATA_FIELD);
            Object object=null;
            if(!data.toString().isEmpty()) {
                object=getObjectFromJson(data);
            }
            return new Packet(method, object);
        }
        catch(JsonProcessingException e) {
            e.printStackTrace();
        }
        catch(Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Returns object of type T from JsonNode
     */
    private static <T> T getObjectFromJson(JsonNode dataJson) {
        Object object=null;
        if(!dataJson.has(CLASS_FIELD)) {
            return null;
        }
        /** Determine class of object and return with cast*/
        String classField=dataJson.get(CLASS_FIELD).asText();

        /** Lists -> All lists are converted into ArrayLists*/
        if(classField.startsWith(LIST_CLASS)) {
            try {
                String[] listType=classField.split(SEPERATOR);
                if(listType.length<2) {
                    return (T) new ArrayList<>();
                }
                Class type=Class.forName(listType[1]);
                String json=dataJson.get(DATA_FIELD).toString();
                List<Object> list=new ArrayList<>();
                ArrayNode array=(ArrayNode) mapper.readTree(json);
                for(JsonNode item : array) {
                    Object o=mapper.readValue(item.toString(), type);
                    list.add(o);
                }
                return (T) list;
            }
            catch(JsonProcessingException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        /** Single objects*/
        else {
            Class type=null;
            try {
                type=Class.forName(classField);
                /** Read primitive types (String,Integer,...)*/
                if(dataJson.has(PRIMITIVE_FIELD)) {
                    object=mapper.readValue(dataJson.get(PRIMITIVE_FIELD).toString(), type);
                }
                else {
                    object=mapper.readValue(dataJson.toString(), type);
                }
                return (T) object;
            }
            catch(ClassNotFoundException | JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getJsonFromObject(Object object) {
        ObjectMapper mapper=new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String jsonString="";
        try {
            ObjectNode node;
            /** Wrap primitive types into primitive field*/
            if(isPrimitiveOrWrapper(object.getClass())) {
                node=mapper.createObjectNode().put(PRIMITIVE_FIELD, object.toString());
            }
            else {
                node=mapper.convertValue(object, ObjectNode.class);
            }
            /** Provide class of object*/
            node.put(CLASS_FIELD, object.getClass().getTypeName());
            jsonString=mapper.writeValueAsString(node);
        }
        catch(IOException e) {
            e.printStackTrace();
            return null;
        }
        return jsonString;
    }

    public static boolean isPrimitiveOrWrapper(Class<?> type) {
        return (type==Double.class || type==Float.class || type==Long.class ||
                type==Integer.class || type==Short.class || type==Character.class ||
                type==Byte.class || type==Boolean.class || type==String.class);
    }

    public static <T> T getDatafromPacketJson(String json) {
        try {
            /** Get alls Key-Value pairs of JSON String*/
            JsonNode packetJson=mapper.readTree(json);
            if(!packetJson.has(DATA_FIELD)) {
                return null;
            }
            JsonNode dataJson=packetJson.get(DATA_FIELD);
            return getObjectFromJson(dataJson);
        }
        catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
