package common.data;

import common.Util;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Objects;

@XmlRootElement
public class Message {
    private boolean translate;
    private String text;

    private Timestamp timestamp;
    private String sender;
    private String receiver;

    private String originalText;
    private String originalLang;
    private HashMap<String,String> translations;

    public Message() {
        translations=new HashMap<>();
        originalLang=null;
        originalText=null;
    }

    public Message(boolean translate, String text, Timestamp timestamp, String sender, String receiver) {
        this();
        this.translate = translate;
        this.text = text;
        this.timestamp = timestamp;
        this.sender = sender;
        this.receiver = receiver;
    }

    public Message(String text, String timestamp, String sender, String receiver) {
        this(false, text, (Timestamp) null, sender, receiver);
        try {
            this.timestamp = new Timestamp(Util.dateFormat.parse(timestamp).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @XmlTransient
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

    @XmlElement(required=true)
    @XmlJavaTypeAdapter(TimestampAdapter.class)
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

    @XmlTransient
    public HashMap<String, String> getTranslations() {
        return translations;
    }

    public void setTranslations(HashMap<String, String> translations) {
        this.translations=translations;
    }

    public void putTranslation(String key, String value) {
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
                ", timestamp='" + Util.dateFormat.format(timestamp) + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                (originalLang!=null ? (", originalLang: '" + originalLang + "', originalTxt: '" + originalText + "'") : "") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if(this==o) {
            return true;
        }
        if(o==null || getClass()!=o.getClass()) {
            return false;
        }
        Message message=(Message) o;
        return Objects.equals(text, message.text) &&
                Objects.equals(Util.dateFormat.format(timestamp), Util.dateFormat.format(message.timestamp)) &&
                Objects.equals(sender, message.sender) &&
                Objects.equals(receiver, message.receiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, timestamp, sender, receiver);
    }

    public static class TimestampAdapter extends XmlAdapter<String, Timestamp> {

        public TimestampAdapter() {
        }

        @Override
        public String marshal(Timestamp v) throws Exception {
            return Util.dateFormat.format(v);
        }

        @Override
        public Timestamp unmarshal(String v) throws Exception {
            return new Timestamp(Util.dateFormat.parse(v).getTime());
        }

    }
}
