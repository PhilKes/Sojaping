package common.data;

import static common.JsonHelper.getJsonFromPacket;

/** Packaging object to send JSON with context and data object */
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

    /** Automatically cast data to T */
    public <T> T getData() {
        return (T)data;
    }
    public void setData(Object data) {
        this.data=data;
    }

    public String getJson(){
        return getJsonFromPacket(this);

    }

    @Override
    public String toString() {
        int l=context.length();
        int tabs=(l / 4);
        if(l % 4>0) {
            tabs++;
        }
        String s=" " + context;
        for(int i=0; i<5 - tabs; i++) {
            s+="\t";
        }
        s+="data= " + data;
        return s;
    }
}
