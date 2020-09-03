package blok2.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * This class is used to send emails. The credentials need to be
 * set in the properties file.
 */
//
//@Service
//public class EmailService {
//
//    @Autowired
//    private JavaMailSender emailSender;
//
////    public EmailService(JavaMailSender mailSender) {
////        this.mailSender = mailSender;
////    }
//
//    public void sendMessage(String to, String subject, String text) {
//        SimpleMailMessage msg = new SimpleMailMessage();
//        msg.setTo(to);
//        msg.setSubject(subject);
//        msg.setText(text);
//        emailSender.send(msg);
//    }
//
//    public void sendMessages(String subject, String text, String... to) {
//        SimpleMailMessage[] messages = new SimpleMailMessage[to.length];
//        for (int i = 0; i < to.length; i++) {
//            SimpleMailMessage msg = new SimpleMailMessage();
//            msg.setTo(to[i]);
//            msg.setText(text);
//            msg.setSubject(subject);
//            messages[i] = msg;
//        }
//        emailSender.send(messages);
//    }
//
//    // for testing purposes
//    void changeMailSenderProperties(int port, String host, String from) {
//        JavaMailSenderImpl impl = (JavaMailSenderImpl) emailSender;
//        impl.setPort(port);
//        impl.setHost(host);
//        impl.setUsername(from);
//        emailSender = impl;
//    }
//
//}
