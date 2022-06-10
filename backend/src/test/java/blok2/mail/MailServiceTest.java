package blok2.mail;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.helpers.Institution;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.reservables.Location;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

public class MailServiceTest extends BaseTest {

    // This email address was created as dummy "sending address" for all purposes during the
    // bachelor's project (how this project started). This address can now be used as dummy
    // "target address" while performing mailing unit tests.
    private static final String TARGET_MAIL_ADDRESS = "blok2.bach@gmail.com";

    @Autowired
    private MailService mailService;

    @Override
    public void populateDatabase() {

    }

    @Test
    public void exampleMailTest() throws MessagingException, UnsupportedEncodingException {
        mailService.sendTestMail(TARGET_MAIL_ADDRESS);
    }

    @Test
    public void exampleOpeningHoursOverviewMailTest() throws MessagingException, UnsupportedEncodingException {
        mailService.sendOpeningHoursOverviewMail(TARGET_MAIL_ADDRESS, 2020, 1);
        mailService.sendOpeningHoursOverviewMail(TARGET_MAIL_ADDRESS, 2020, 52);
        mailService.sendOpeningHoursOverviewMail(TARGET_MAIL_ADDRESS, 2021, 3);
        mailService.sendOpeningHoursOverviewMail(TARGET_MAIL_ADDRESS, 2021, 4);
        mailService.sendOpeningHoursOverviewMail(TARGET_MAIL_ADDRESS, 2021, 5);
    }

    @Test
    public void exampleLocationCreatedMailTest() throws MessagingException, UnsupportedEncodingException {
        Authority authority = new Authority(1, "Test autoriteit", "Test authority");
        Building building = new Building(1, "Test gebouw", "Test building", Institution.UGent);
        Location location = TestSharedMethods.testLocation(authority, building);
        mailService.sendNewLocationMessage(TARGET_MAIL_ADDRESS, "Bram Van de Walle", location);
    }

}
