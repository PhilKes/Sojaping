package common.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.sql.Timestamp;

/*@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "_class")*/
public class Message {
    //private String sendLanguage;
    //private String receiveLanguage;
    private boolean translate;
    private String text;
    private Timestamp timestamp;


    public Message(/*String sendLanguage, String receiveLanguage,*/ boolean translate, String text, Timestamp timestamp) {
        /*this.sendLanguage = sendLanguage;
        this.receiveLanguage = receiveLanguage;*/
        this.translate = translate;
        this.text = text;
        this.timestamp = timestamp;
    }
    /*
    public String getSendLanguage() {
        return sendLanguage;
    }

    public void setSendLanguage(String sendLanguage) {
        this.sendLanguage = sendLanguage;
    }

    public String getReceiveLanguage() {
        return receiveLanguage;
    }

    public void setReceiveLanguage(String receiveLanguage) {
        this.receiveLanguage = receiveLanguage;
    }
    */
    public boolean isTranslate() {
        return translate;
    }

    public void setTranslate(boolean translate) {
        this.translate = translate;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
