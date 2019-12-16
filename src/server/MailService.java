package server;

import com.sun.org.apache.xerces.internal.xs.StringList;
import common.Constants;
import common.data.Profile;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.util.List;
import java.util.Properties;

public class MailService {
    private static final String SMTP_HOST_NAME = "smtp.web.de";
    private static final String SMTP_PORT_NR = "587";
    private static final String SMTP_AUTH_USER = "SOJA_Ping@web.de";
    private static final String SMTP_AUTH_PWD = "student123!";
    private Session smtpSession;

    public MailService(){
        Properties properties = System.getProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", SMTP_HOST_NAME);
        properties.put("mail.smtp.port", SMTP_PORT_NR);
        properties.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        //if debug is needed, add this
        //properties.put("mail.debug", "true");
        smtpSession = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_AUTH_USER, SMTP_AUTH_PWD);
            }
        });
    }

    public boolean sendInviteMail(String receiver, Profile sender) {
        MimeMessage message = new MimeMessage(smtpSession);
        try {
            message.setFrom(new InternetAddress("SOJA_Ping@web.de"));
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(receiver));
            message.setSubject("SOJA-Ping Messenger");
            message.setContent(" Hey, " +
                    "\n\n you have been invited to SOJA-Ping by "+ sender.getUserName()+". " +
                    "\n SOJA-Ping is as easy to Use Chat-Application for your Computer." +
                    "\n\n get it for free at:" +
                    "\n https://github.com/PhilKes/Sojaping/releases", "text/plain");
            //Transport transport = smtpSession.getTransport();
            //transport.connect();
            //transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
            //transport.close();
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        MailService mailService = new MailService();
        Profile testProfile = new Profile("jan", 0, null, null, null);
        mailService.sendInviteMail("Jan.Komposch@gmail.com",testProfile);
    }
}
