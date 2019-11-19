package common.data;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Message {
    //private String sendLanguage;
    //private String receiveLanguage;
    private boolean translate;
    private String text;
    private Timestamp timestamp;
    private String sender;
    private String receiver;

    public Message() {

    }
    public Message(/*String sendLanguage, String receiveLanguage,*/ boolean translate, String text, Timestamp timestamp, String sender, String receiver) {
        /*this.sendLanguage = sendLanguage;
        this.receiveLanguage = receiveLanguage;*/
        this.translate = translate;
        this.text = text;
        this.timestamp = timestamp;
        this.sender = sender;
        this.receiver = receiver;
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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    @Override
    public String toString() {
        return "Message{" +
                "text='" + text + '\'' +
                ", timestamp='"  + new SimpleDateFormat("HH:mm:ss \n dd-MM-yy").format(timestamp) + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                '}';
    }
}
