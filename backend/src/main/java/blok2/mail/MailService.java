package blok2.mail;

import blok2.model.reservables.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;

@Service
public class MailService {

    private final TemplateEngine templateEngine;
    private final JavaMailSender mailSender;

    public static final String EXAMPLE_MAIL_TEMPLATE_URL = "mail/example_mail";
    public static final String OPENING_HOURS_OVERVIEW_TEMPLATE_URL = "mail/opening_hours_overview";
    public static final String ADMIN_VALIDATION_FOR_NEW_LOCATION_REQUESTED_TEMPLATE_URL = "mail/location_created_validation";

    @Autowired
    public MailService(TemplateEngine templateEngine, JavaMailSender mailSender) {
        this.templateEngine = templateEngine;
        this.mailSender = mailSender;
    }

    public void sendTestMail(String target) throws MessagingException {
        Context ctx = new Context();
        String title = "[Werk- en Studieplekken] Testmail";

        ctx.setVariable("name", "John Doe");
        ctx.setVariable("title", title);

        sendMail(target, "[Werk- en Studieplekken] Testmail", ctx, EXAMPLE_MAIL_TEMPLATE_URL);
    }

    public void sendOpeningHoursOverviewMail(String target, int year, int week) throws MessagingException {
        Context ctx = new Context();

        LocalDate from = LocalDate.ofYearDay(year, 50)
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = from.plusDays(6);

        String fromFormatted = from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String toFormatted = to.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String title = String.format("[Werk- en Studieplekken] Openingsuren %s tot %s", fromFormatted, toFormatted);
        String overviewURL = String.format("https://studieplekken.ugent.be/opening/overview/%d/%d", year, week);

        ctx.setVariable("title", title);
        ctx.setVariable("fromFormatted", fromFormatted);
        ctx.setVariable("toFormatted", toFormatted);
        ctx.setVariable("overviewURL", overviewURL);
        ctx.setVariable("year", year);
        ctx.setVariable("week", week);

        sendMail(target, title, ctx, OPENING_HOURS_OVERVIEW_TEMPLATE_URL);
    }

    public void sendNewLocationMessage(String target, String adminName, Location location) throws MessagingException {
        Context ctx = new Context();
        String title = "[Werk- en Studieplekken] Aanvraag voor nieuwe locatie";

        ctx.setVariable("title", title);
        ctx.setVariable("adminName", adminName);
        ctx.setVariable("locationName", location.getName());
        ctx.setVariable("numberOfSeats", location.getNumberOfSeats());
        ctx.setVariable("buildingName", location.getBuilding().getName());
        ctx.setVariable("authorityName", location.getAuthority().getAuthorityName());

        sendMail(target, title, ctx, ADMIN_VALIDATION_FOR_NEW_LOCATION_REQUESTED_TEMPLATE_URL);
    }

    private void sendMail(String target, String subject, Context ctx, String templateFilename) throws MessagingException {
        sendMail(target, subject, null, null, ctx, templateFilename);
    }

    private void sendMail(String target, String subject, String attachmentName, DataSource attachment,
                          Context ctx, String templateFilename) throws MessagingException {
        // Prepare message using a Spring helper
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, attachment != null, "UTF-8");

        message.setFrom("no-reply@studieplekken.ugent.be");
        message.setTo(target);
        message.setSubject(subject);

        // Create the HTML body using Thymeleaf
        String htmlContent = this.templateEngine.process(templateFilename, ctx);
        message.setText(htmlContent, true);

        if (attachment != null) {
            message.addAttachment(attachmentName, attachment);
        }

        mailSender.send(mimeMessage);
    }

}
