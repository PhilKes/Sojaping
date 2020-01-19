package client;

import client.presentation.FXUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

/**
 * XML Parser/Reader for local MessageStores
 */
public class MessageParser {

    private JAXBContext jaxbContext;
    private Marshaller jaxbMarshaller;
    private Unmarshaller jaxbUnmarshaller;

    public MessageParser() {
        try {
            jaxbContext=JAXBContext.newInstance(MessageStore.class);
            jaxbMarshaller=jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbUnmarshaller=jaxbContext.createUnmarshaller();
        }
        catch(JAXBException e) {
            e.printStackTrace();
        }
    }

    public MessageStore getMessageStore(String userName) {
        MessageStore store=null;
        InputStream in=FXUtil.getMessageStoreFileStream(userName);
        if(in==null) {
            System.out.println("Empty MessageStore");
            return new MessageStore();
        }
        try {
            store=(MessageStore) jaxbUnmarshaller.unmarshal(in);
            //System.out.println(store);

        }
        catch(JAXBException e) {
            e.printStackTrace();
        }
        return store;
    }

    public boolean storeMessageStore(MessageStore store, boolean merge) {
        System.out.println("Storing MessageStore of " + store.getUserName());
        MessageStore fileStore=store;
        if(merge) {
            fileStore=getMessageStore(store.getUserName());
            fileStore.getMessages().addAll(store.getMessages());
        }
        try {
            jaxbMarshaller.marshal(fileStore, FXUtil.getMessageStoreFileOutStream(store.getUserName()));
            //jaxbMarshaller.marshal(fileStore, System.out);
        }
        catch(JAXBException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean resetMessageStore(String userName) {
        MessageStore store=new MessageStore();
        store.setUserName(userName);
        storeMessageStore(store, false);
        return true;
    }
}
