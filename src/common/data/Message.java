package common.data;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;

public class Message {
    private boolean translate;
    private String text;
    private Timestamp timestamp;
    private String sender;
    private String receiver;

    //TODO Extra class "TranslatedMessage"?
    // INHERIT:
    private String originalText;
    private String originalLang;
    private HashMap<String,String> translations;


    public Message() {
        translations=new HashMap<>();
        originalLang=null;
        originalText=null;
    }

    public Message( boolean translate, String text, Timestamp timestamp, String sender, String receiver) {
        this();
        this.translate = translate;
        this.text = text;
        this.timestamp = timestamp;
        this.sender = sender;
        this.receiver = receiver;
    }

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

    public String getOriginalLang() {
        return originalLang;
    }
    public void setOriginalLang(String originalLang) {
        this.originalLang=originalLang;
    }

    public HashMap<String, String> getTranslations() {
        return translations;
    }
    public void setTranslations(HashMap<String, String> translations) {
        this.translations=translations;
    }

    public void putTranslation(String key,String value){
        this.translations.put(key,value);
    }

    public String getTranslation(String key) {
        if(this.translations.containsKey(key))
            return this.translations.get(key);
        return null;
    }

    public String getOriginalText() {
        return originalText;
    }
    public void setOriginalText(String originalText) {
        this.originalText=originalText;
    }

    @Override
    public String toString() {
        return "Message{" +
                "text='" + text + '\'' +
                ", timestamp='"  + new SimpleDateFormat("HH:mm:ss \n dd-MM-yy").format(timestamp) + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                (originalLang!=null ? (", originalLang: '" + originalLang + "', originalTxt: '" + originalText + "'") : "") +
                '}';
    }
}
