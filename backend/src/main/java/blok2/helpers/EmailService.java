package blok2.helpers;

import blok2.daos.ICalendarPeriodDao;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.reservables.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Service
public class EmailService {


    private final JavaMailSender emailSender;
    private final CSVHelper helper;
    private final ICalendarPeriodDao calendarPeriodDao;


    @Autowired
    public EmailService(JavaMailSender emailSender, CSVHelper helper, ICalendarPeriodDao calendarPeriodDao) {
        this.emailSender = emailSender;
        this.helper = helper;
        this.calendarPeriodDao = calendarPeriodDao;
    }

    public void sendCalendarPeriodsMessage(String target) throws SQLException, IOException, MessagingException {
        String content = "A CSV of future building reservations is attached for approval.\n\n";
        content += "This is an automated message. If you are not the intended recipient, or have other concerns in this message, please mail dsa@ugent.be";

        List<CalendarPeriod> periods = calendarPeriodDao.getCalendarPeriodsInWeek(LocalDate.now().plusWeeks(3));
        DataSource attachment = new ByteArrayDataSource(helper.calendarPeriodCSV(periods), "text/csv");
        sendAttachmentMessage(target, "Reservations of BlokAt", content, "file.csv", attachment);
    }

    public void sendNewLocationMessage(String target, Location location) throws IOException, MessagingException {
        String content = "A new BlokAt study location has been requested. Please validate the usability of this location, and forward this to an admin (dsa@ugent.be)\n\n";
        content += "Location name: " + location.getName() + "\n";
        content += "Building: " + location.getBuilding().getName() + "\n";
        content += "Address: " + location.getBuilding().getAddress() + "\n";
        content += "Authority: " + location.getAuthority().getAuthorityName() + "\n";

        if(location.getNumberOfSeats() > 0)
            content += "Proposed number of seats: " + location.getNumberOfSeats() + "\n";
        if(location.getNumberOfLockers() > 0)
            content += "Proposed number of lockers: " + location.getNumberOfLockers() + "\n";


        sendSimpleMessage(target, "BlokAt: New Location", content);
    }


    private void sendSimpleMessage(String target, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(target);
        message.setSubject(subject);
        message.setText(content);
        message.setFrom("bot@blokat.ugent.be");
        emailSender.send(message);

        System.out.println(message);
        System.out.println("Sent messsage!");
    }

    private void sendAttachmentMessage(String target, String subject, String content, String attachmentName, DataSource attachment) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(target);
        helper.setSubject(subject);
        helper.setText(content);
        helper.setFrom("bot@blokat.ugent.be");
        helper.addAttachment(attachmentName, attachment);
        emailSender.send(message);

        System.out.println(message);
        System.out.println("Sent messsage!");
    }
}