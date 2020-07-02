package be.ugent.blok2.helpers;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

/**
 * This class is used to send emails. The credentials need to be
 * set in the properties file.
 */
@Component
public class EmailService {

    private JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMessage(String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }

    public void sendMessages(String subject, String text, String... to){
        SimpleMailMessage[] messages = new SimpleMailMessage[to.length];
        for(int i=0; i< to.length; i++){
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to[i]);
            msg.setText(text);
            msg.setSubject(subject);
            messages[i] = msg;
        }
        mailSender.send(messages);
    }

    // for testing purposes
    void changeMailSenderProperties(int port, String host, String from){
        JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;
        impl.setPort(port);
        impl.setHost(host);
        impl.setUsername(from);
        mailSender = impl;
    }

}
