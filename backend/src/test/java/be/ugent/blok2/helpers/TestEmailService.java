package be.ugent.blok2.helpers;

import be.ugent.blok2.model.penalty.PenaltyEvent;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.Message;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class TestEmailService {

    @Autowired
    private EmailService emailService;

    // Tried using GreenMail for automatic testing of mail sending but didn't work so it's just a manual test for now
    // insert your email in variable below and check if you got the expected mails

    private String emailAddress =  "test@exmaple.com";

    //private GreenMail testSmtp;

    @Before
    public void init(){
        //testSmtp = new GreenMail(ServerSetupTest.SMTP);
        //testSmtp.start();

        //emailService.changeMailSenderProperties(3025, "localhost", "from@localhost.com");
    }

    @After
    public void cleanup(){
        //testSmtp.stop();
    }

    @Test
    public void testSendMessage() {
        String testSubject = "Testsubject";
        String testBody = "Test body";

        emailService.sendMessage(emailAddress, testSubject, testBody);
        // You should have got a new email with subject Testsubject and body Test Body at the end of this test

        //Message[] messages = testSmtp.getReceivedMessages();
        //assertEquals(messages.length, 1);
    }

    @Test
    public void testSendMessages() {
        emailService.sendMessages("Test multiple messages", "body of multiple messages", emailAddress, emailAddress);
        // After this you should have gotten 2 emails with contents above, if you want to test for different emailaddresses you can change
        // one into another mail address, or add one as a last parameter
    }


}
