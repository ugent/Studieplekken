package blok2.mail;

import blok2.model.calendar.CalendarPeriod;
import blok2.model.reservables.Location;
import blok2.model.reservations.LocationReservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

@Service
public class MailService {

    public static final Logger logger = LoggerFactory.getLogger(MailService.class);

    private final TemplateEngine templateEngine;
    private final JavaMailSender mailSender;

    public static final String EXAMPLE_MAIL_TEMPLATE_URL = "mail/example_mail";
    public static final String OPENING_HOURS_OVERVIEW_TEMPLATE_URL = "mail/opening_hours_overview";
    public static final String ADMIN_VALIDATION_FOR_NEW_LOCATION_REQUESTED_TEMPLATE_URL = "mail/location_created";
    public static final String STUDENT_DID_NOT_ATTEND = "mail/not_attended";

    public static final String NO_REPLY_SENDER = "no-reply@dsa.ugent.be";

    public static final String URL_DEVELOPMENT = "https://localhost:4200";
    public static final String URL_PRODUCTION = "https://studieplekken.ugent.be";

    private final Set<String> springProfilesActive;

    @Autowired
    public MailService(TemplateEngine templateEngine, JavaMailSender mailSender, Environment env) {
        this.templateEngine = templateEngine;
        this.mailSender = mailSender;

        springProfilesActive = new TreeSet<>();
        Collections.addAll(springProfilesActive, env.getActiveProfiles());
    }

    /**
     * Only use this method in test environments because the production/development
     * environment is not checked.
     */
    @Deprecated
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
        String baseURL = springProfilesActive.contains("dev")
                ? URL_DEVELOPMENT
                : URL_PRODUCTION;
        String overviewURL = String.format("%s/opening/overview/%d/%d", baseURL, year, week);

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

    // *************************************************
    // *  Methods for mailing to unattended students   *
    // *************************************************/

    public void sendMailToUnattendedStudent(String target, LocationReservation locationReservation,
                                            CalendarPeriod calendarPeriod) throws MessagingException {
        Context ctx = new Context();
        String title = prepareMailToUnattendedStudent(locationReservation, calendarPeriod, ctx);
        ctx.setVariable("addressing", locationReservation.getUser().getFirstName());
        sendMail(target, title, ctx, STUDENT_DID_NOT_ATTEND);
    }

    private String prepareMailToUnattendedStudent(LocationReservation locationReservation,
                                                  CalendarPeriod calendarPeriod, Context ctx) {
        String title = "[Werk- en Studieplekken] Afwezigheid op gereserveerd tijdslot";

        ctx.setVariable("title", title);
        ctx.setVariable("studieplek", calendarPeriod.getLocation().getName());

        LocalTime startTime = calendarPeriod
                .getOpeningTime()
                .plusMinutes((long) calendarPeriod.getTimeslotLength() * locationReservation.getTimeslot().getTimeslotSeqnr());

        LocalTime endTime = startTime.plusMinutes(calendarPeriod.getTimeslotLength());

        ctx.setVariable("startTime", startTime.format(DateTimeFormatter.ISO_TIME));
        ctx.setVariable("endTime", endTime.format(DateTimeFormatter.ISO_TIME));

        return title;
    }

    // ********************************************
    // *  Methods for actually sending the mail   *
    // ********************************************/

    /**
     * Send a mail for which the content is defined by the templateFileName and the context to one recipient.
     */
    private void sendMail(String target, String subject, Context ctx, String templateFileName) throws MessagingException {
        if (!allowedToSendMailByEnvironment(target, templateFileName, subject))
            return;

        logger.info(String.format("Sending mail with template file name '%s' to '%s' with subject '%s'",
                templateFileName, target, subject));

        MimeMessageHelper messageHelper = prepareMimeMessageHelper(subject, templateFileName, ctx);
        messageHelper.setTo(target);

        Thread thread = new Thread(() -> mailSender.send(messageHelper.getMimeMessage()));
        thread.start();
    }

    /**
     * Send a mail for which the content is defined by the templateFileName and the context to multiple recipients.
     */
    private void sendMail(String[] targets, String subject, Context ctx, String templateFileName) throws MessagingException {
        if (!allowedToSendMailByEnvironment(targets, templateFileName, subject))
            return;

        logger.info(String.format("Sending mail with template file name '%s' to '%s' with subject '%s'",
                templateFileName, Arrays.toString(targets), subject));

        MimeMessageHelper messageHelper = prepareMimeMessageHelper(subject, templateFileName, ctx);
        messageHelper.setTo(targets);

        Thread thread = new Thread(() -> mailSender.send(messageHelper.getMimeMessage()));
        thread.start();
    }

    private MimeMessageHelper prepareMimeMessageHelper(String subject, String templateFileName, Context ctx)
            throws MessagingException {
        // Prepare message using a Spring helper
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

        messageHelper.setFrom(NO_REPLY_SENDER);
        messageHelper.setSubject(subject);

        // Create the HTML body using Thymeleaf
        String htmlContent = this.templateEngine.process(templateFileName, ctx);
        messageHelper.setText(htmlContent, true);

        return messageHelper;
    }

    // ************************
    // *  Auxiliary methods   *
    // ************************/

    /**
     * Only if the profile 'mail' is included in spring.profiles.active, mails are allowed
     * to be sent. Otherwise they are blocked.
     */
    private boolean allowedToSendMailByEnvironment(String target, String templateFileName, String subject) {
        if (springProfilesActive.contains("mail"))
            return true;

        logger.info(String.format("Blocked sending mail to '%s' with template file name '%s' and " +
                        "subject '%s' because 'mail' is not an active profile.", target, templateFileName, subject));

        return false;
    }

    private boolean allowedToSendMailByEnvironment(String[] targets, String templateUrl, String subject) {
        return allowedToSendMailByEnvironment(Arrays.toString(targets), templateUrl, subject);
    }

}
