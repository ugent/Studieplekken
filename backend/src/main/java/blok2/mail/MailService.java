package blok2.mail;

import blok2.model.reservables.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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

    // *****************************************************
    // *  Methods for mailing the opening hours overview   *
    // *****************************************************/

    public void sendOpeningHoursOverviewMail(String target, int year, int week) throws MessagingException {
        Context ctx = new Context();
        String title = prepareOpeningHoursOverviewMail(ctx, year, week);
        sendMail(target, title, ctx, OPENING_HOURS_OVERVIEW_TEMPLATE_URL);
    }

    public void sendOpeningHoursOverviewMail(String[] targets, int year, int week) throws MessagingException {
        Context ctx = new Context();
        String title = prepareOpeningHoursOverviewMail(ctx, year, week);
        sendMail(targets, title, ctx, OPENING_HOURS_OVERVIEW_TEMPLATE_URL);
    }

    private String prepareOpeningHoursOverviewMail(Context ctx, int year, int week) {
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

        return title;
    }

    // **********************************************************
    // *  Methods for mailing that a new location was created   *
    // **********************************************************/

    public void sendNewLocationMessage(String target, String adminName, Location location) throws MessagingException {
        Context ctx = new Context();
        String title = prepareNewLocationMail(location, ctx);
        ctx.setVariable("addressing", adminName);
        sendMail(target, title, ctx, ADMIN_VALIDATION_FOR_NEW_LOCATION_REQUESTED_TEMPLATE_URL);
    }

    public void sendNewLocationMessage(String[] targets, Location location) throws MessagingException {
        Context ctx = new Context();
        String title = prepareNewLocationMail(location, ctx);
        ctx.setVariable("addressing", "admin van de Werk- en Studieplekken applicatie");
        sendMail(targets, title, ctx, ADMIN_VALIDATION_FOR_NEW_LOCATION_REQUESTED_TEMPLATE_URL);
    }

    private String prepareNewLocationMail(Location location, Context ctx) {
        String title = "[Werk- en Studieplekken] Aanvraag voor nieuwe locatie";

        ctx.setVariable("title", title);
        ctx.setVariable("locationName", location.getName());
        ctx.setVariable("numberOfSeats", location.getNumberOfSeats());
        ctx.setVariable("buildingName", location.getBuilding().getName());
        ctx.setVariable("authorityName", location.getAuthority().getAuthorityName());

        return title;
    }

    // ********************************************
    // *  Methods for actually sending the mail   *
    // ********************************************/

    /**
     * Send a mail for which the content is defined by the templateFileName and the context to one recipient.
     */
    private void sendMail(String target, String subject, Context ctx, String templateFileName) throws MessagingException {
        MimeMessageHelper messageHelper = prepareMimeMessageHelper(subject, templateFileName, ctx);
        messageHelper.setTo(target);
        mailSender.send(messageHelper.getMimeMessage());
    }

    /**
     * Send a mail for which the content is defined by the templateFileName and the context to multiple recipients.
     */
    private void sendMail(String[] targets, String subject, Context ctx, String templateFileName) throws MessagingException {
        MimeMessageHelper messageHelper = prepareMimeMessageHelper(subject, templateFileName, ctx);
        messageHelper.setTo(targets);
        mailSender.send(messageHelper.getMimeMessage());
    }

    private MimeMessageHelper prepareMimeMessageHelper(String subject, String templateFileName, Context ctx)
            throws MessagingException {
        // Prepare message using a Spring helper
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

        messageHelper.setFrom("no-reply@studieplekken.ugent.be");
        messageHelper.setSubject(subject);

        // Create the HTML body using Thymeleaf
        String htmlContent = this.templateEngine.process(templateFileName, ctx);
        messageHelper.setText(htmlContent, true);

        return messageHelper;
    }

}
