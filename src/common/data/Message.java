package common.data;

import java.sql.Timestamp;

public class Message {
    //private String sendLanguage;
    //private String receiveLanguage;
    private boolean translate;
    private String text;
    private Timestamp timestamp;
    Account sender;
    Account receiver;

    public Account getSender() {
        return sender;
    }

    public void setSender(Account sender) {
        this.sender = sender;
    }

    public Account getReceiver() {
        return receiver;
    }

    public void setReceiver(Account receiver) {
        receiver = receiver;
    }
//Todo Sender / Receiver ?


    public Message(/*String sendLanguage, String receiveLanguage,*/ boolean translate, String text, Timestamp timestamp, Account sender, Account receiver) {
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
}
