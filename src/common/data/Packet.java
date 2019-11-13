package common.data;

import static common.JsonHelper.getJsonFromPacket;

public class Packet {

    private String context;
    private Object data;

    public Packet() {
        context="";
        data=null;
    }

    public Packet(String context, Object data) {
        this.context=context;
        this.data=data;
    }

    public String getContext() {
        if(context==null)
            return "";
        return context;
    }

    public void setContext(String context) {
        this.context=context;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data=data;
    }
    public String getJson(){
        return getJsonFromPacket(context,data);
    }

    @Override
    public String toString() {
        return "Packet{" +
                "context='" + context + '\'' +
                ", data=" + data +
                '}';
    }
}
