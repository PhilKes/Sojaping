package server;

import common.Constants;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.util.Properties;

public class MailService {
    private static final String SMTP_HOST_NAME = "smtp.web.de";
    private static final String SMTP_PORT_NR = "587";
    private static final String SMTP_AUTH_USER = "SOJA_Ping@web.de";
    private static final String SMTP_AUTH_PWD = "student123!";

    public static void main(String[] args) {
        MailService mailService = new MailService();
        mailService.sendInviteMail("Jan.Komposch@gmail.com");
    }

    public boolean sendInviteMail(String receiver) {
        Session smtpSession = createSMTPconnection();
        MimeMessage message = new MimeMessage(smtpSession);
        try {
            Transport transport = smtpSession.getTransport();
            message.setSubject("Test");
            message.setContent("This is a test", "text/plain");
            message.setFrom(new InternetAddress("SOJA_Ping@web.de"));
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(receiver));
            transport.connect();
            transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
            transport.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    ;

    private Session createSMTPconnection() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", SMTP_HOST_NAME);
        properties.put("mail.smtp.port", SMTP_PORT_NR);
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.debug", "true");

        Session mailSMTPsession = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_AUTH_USER, SMTP_AUTH_PWD);
            }
        });
        return mailSMTPsession;

    }
}
