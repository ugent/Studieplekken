package blok2.mail;

import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
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
import java.io.UnsupportedEncodingException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class MailService {

    public static final Logger logger = LoggerFactory.getLogger(MailService.class);

    private final TemplateEngine templateEngine;
    private final JavaMailSender mailSender;

    public static final String EXAMPLE_MAIL_TEMPLATE_URL = "mail/example_mail";
    public static final String OPENING_HOURS_OVERVIEW_TEMPLATE_URL = "mail/opening_hours_overview";
    public static final String ADMIN_VALIDATION_FOR_NEW_LOCATION_REQUESTED_TEMPLATE_URL = "mail/location_created";
    public static final String STUDENT_DID_NOT_ATTEND_TEMPLATE_URL = "mail/not_attended";
    public static final String RESERVATION_NOTIFICATION_TEMPLATE_URL = "mail/reservation_notification";
    public static final String RESERVATION_SLOT_DELETED_TEMPLATE_URL = "mail/reservation_slot_deleted";
    public static final String RESERVATION_CONFIRMATION_PAST24HRS_URL = "mail/reservation_confirmation_past24hrs";

    public static final String NO_REPLY_SENDER = "info@studieplekken.ugent.be";
    public static final String NO_REPLY_SENDER_NAME = "Studieplekken";

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
    public void sendTestMail(String target) throws MessagingException, UnsupportedEncodingException {
        Context ctx = new Context();
        String title = "[Studieplekken] Testmail";

        ctx.setVariable("name", "John Doe");
        ctx.setVariable("title", title);

        sendMail(target, "[Studieplekken] Testmail", ctx, EXAMPLE_MAIL_TEMPLATE_URL);
    }

    // *****************************************************
    // *  Methods for mailing the opening hours overview   *
    // *****************************************************/

    public void sendOpeningHoursOverviewMail(String target, int year, int week) throws MessagingException, UnsupportedEncodingException {
        Context ctx = new Context();
        String title = prepareOpeningHoursOverviewMail(ctx, year, week);
        sendMail(target, title, ctx, OPENING_HOURS_OVERVIEW_TEMPLATE_URL);
    }

    public void sendOpeningHoursOverviewMail(String[] targets, int year, int week) throws MessagingException, UnsupportedEncodingException {
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
        String title = String.format("[Studieplekken] Openingsuren %s tot %s", fromFormatted, toFormatted);
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

    public void sendNewLocationMessage(String target, String adminName, Location location) throws MessagingException, UnsupportedEncodingException {
        Context ctx = new Context();
        String title = prepareNewLocationMail(location, ctx);
        ctx.setVariable("addressing", adminName);
        sendMail(target, title, ctx, ADMIN_VALIDATION_FOR_NEW_LOCATION_REQUESTED_TEMPLATE_URL);
    }

    public void sendNewLocationMessage(String[] targets, Location location) throws MessagingException, UnsupportedEncodingException {
        Context ctx = new Context();
        String title = prepareNewLocationMail(location, ctx);
        ctx.setVariable("addressing", "admin van de Studieplekken applicatie");
        sendMail(targets, title, ctx, ADMIN_VALIDATION_FOR_NEW_LOCATION_REQUESTED_TEMPLATE_URL);
    }

    private String prepareNewLocationMail(Location location, Context ctx) {
        String title = "[Studieplekken] Aanvraag voor nieuwe locatie";

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

    public Thread sendMailToUnattendedStudent(String target) throws MessagingException, UnsupportedEncodingException {
        Context ctx = new Context();
        String title = "[Studieplekken] Afwezigheid op gereserveerd tijdslot";
        ctx.setVariable("title", title);
        return sendMail(target, title, ctx, STUDENT_DID_NOT_ATTEND_TEMPLATE_URL);
    }

    // ************************************************************************************************
    // *  Methods for mailing a notification to students that have made a reservation for next week   *
    // ************************************************************************************************/

    public Thread sendReminderToStudentsAboutReservation(String target) throws MessagingException, UnsupportedEncodingException {
        Context ctx = new Context();
        String title = "[Studieplekken] Herinnering reservatie(s) studieplek(ken) volgende week";
        ctx.setVariable("title", title);
        return sendMail(target, title, ctx, RESERVATION_NOTIFICATION_TEMPLATE_URL);
    }

    // *************************************************
    // *  Methods for mailing to unattended students   *
    // *************************************************/

    public Thread sendReservationSlotDeletedMessage(String target, Timeslot timeslot) throws MessagingException, UnsupportedEncodingException {
        Context ctx = new Context();
        String title = "[Studieplekken] Uw gereserveerd tijdslot werd verwijderd";
        ctx.setVariable("title", title);
        ctx.setVariable("date", timeslot.timeslotDate());
        return sendMail(target, title, ctx, RESERVATION_SLOT_DELETED_TEMPLATE_URL);
    }

    // ***************************************************************************************************
    // * Method for mailing a notification to a student of all reservations that were made by them today *
    // **************************************************************************************************/

    public Thread sendMailConfirmingLast24hrsOfReservations(String target, List<MailReservationData> reservationData) throws MessagingException, UnsupportedEncodingException {
        reservationData.sort(Comparator.comparing(o -> o.time));
        // Make a list of all reservation data that has a location not previously mentioned in the list.
        // a.k.a get all locations.
        List<MailReservationData> locationInformation = new ArrayList<>();
        for (MailReservationData mailReservationDatum : reservationData) {
            if (locationInformation.stream().anyMatch(mrd -> mrd.locationName.equals(mailReservationDatum.locationName))) {
                continue; // Skip if locationinfo already in list.
            }
            if (mailReservationDatum.locationReminderEnglish.isEmpty() && mailReservationDatum.locationReminderDutch.isEmpty()) {
                continue; // Skip if locationinfo is empty string
            }
            locationInformation.add(mailReservationDatum);
        }

        Context ctx = new Context();
        String title = "[Studieplekken] Bevestiging van uw reservatie(s)";
        ctx.setVariable("title", title);
        ctx.setVariable("mailReservationData", reservationData);
        ctx.setVariable("locationInformation", locationInformation);
        return sendMail(target, title, ctx, RESERVATION_CONFIRMATION_PAST24HRS_URL);
    }


    // ********************************************
    // *  Methods for actually sending the mail   *
    // ********************************************/

    /**
     * Send a mail for which the content is defined by the templateFileName and the context to one recipient.
     */
    private Thread sendMail(String target, String subject, Context ctx, String templateFileName) throws MessagingException, UnsupportedEncodingException {
        if (!allowedToSendMailByEnvironment(target, templateFileName, subject))
            return new Thread(this::nop);

        logger.info(String.format("Sending mail with template file name '%s' to '%s' with subject '%s'",
                templateFileName, target, subject));

        MimeMessageHelper messageHelper = prepareMimeMessageHelper(subject, templateFileName, ctx);
        messageHelper.setTo(target);

        Thread thread = new Thread(() -> mailSender.send(messageHelper.getMimeMessage()));
        thread.start();
        return thread;
    }

    /**
     * Send a mail for which the content is defined by the templateFileName and the context to multiple recipients.
     */
    private void sendMail(String[] targets, String subject, Context ctx, String templateFileName) throws MessagingException, UnsupportedEncodingException {
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
            throws MessagingException, UnsupportedEncodingException {
        // Prepare message using a Spring helper
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

        messageHelper.setFrom(NO_REPLY_SENDER, NO_REPLY_SENDER_NAME);
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

    private void nop() {

    }

}
