package client;

import common.data.Message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MessageStore {
    @XmlElement
    private String userName;
    @XmlElement
    private List<Message> messages=null;

    public MessageStore() {
        this.messages=new ArrayList<>();
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages=messages;
    }

    public void addMessage(Message msg) {
        messages.add(msg);
    }

    public Message getMessage(int i) {
        return messages.get(i);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName=userName;
    }

    @Override
    public String toString() {
        return "MessageStore{" +
                "messages=" + messages +
                '}';
    }
}