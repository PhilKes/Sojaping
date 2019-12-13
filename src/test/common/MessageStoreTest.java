package test.common;

import common.data.Message;
import common.data.MessageStore;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class MessageStoreTest {
    static File file;
    static JAXBContext jaxbContext;
    static Marshaller jaxbMarshaller;
    static Unmarshaller jaxbUnmarshaller;

    @BeforeClass
    public static void init() {
        try {
            file=new File("", "testMessageStore.xml");
            jaxbContext=JAXBContext.newInstance(MessageStore.class);
            jaxbMarshaller=jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbUnmarshaller=jaxbContext.createUnmarshaller();

        }
        catch(JAXBException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testStoreMessages() {
        MessageStore store=new MessageStore();
        for(int i=0; i<10; i++) {
            Message msg=new Message();
            msg.setText("test" + i);
            msg.setReceiver("phil");
            msg.setSender("irina");
            msg.setTimestamp(new Timestamp(new Date().getTime()));
            store.addMessage(msg);
        }
        try {
            jaxbMarshaller.marshal(store, file);
            jaxbMarshaller.marshal(store, System.out);
        }
        catch(JAXBException e) {
            e.printStackTrace();
        }
        MessageStore store1=null;
        try {
            store1=(MessageStore) jaxbUnmarshaller.unmarshal(file);
            System.out.println(store1);

        }
        catch(JAXBException e) {
            e.printStackTrace();
        }
        for(int i=0; i<store.getMessages().size(); i++) {
            assertEquals(store.getMessages().get(i), store1.getMessages().get(i));
        }
    }

}
