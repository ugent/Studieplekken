package blok2.scheduling;

import blok2.daos.ILocationDao;
import blok2.daos.ILocationReservationDao;
import blok2.helpers.Pair;
import blok2.mail.MailService;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.reservations.LocationReservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    private final ILocationDao locationDao;
    private final ILocationReservationDao locationReservationDao;
    private final MailService mailService;

    private final String[] recipients;

    @Autowired
    public ScheduledTasks(ILocationDao locationDao, ILocationReservationDao locationReservationDao,
                          MailService mailService, Environment env) {
        this.locationDao = locationDao;
        this.locationReservationDao = locationReservationDao;
        this.mailService = mailService;
        recipients = env.getProperty("custom.mailing.recipientsOpeningHoursOverview", String[].class);
    }

    /**
     * Schedule this task to be run every monday at 6 AM. The task is responsible for sending
     * an email to following UGent services (see recipients in application-prod.yml in the property
     * custom.mailing.recipientsOpeningHoursOverview):
     *     - Alarmbeheer - alarmbeheer@ugent.be
     *     - Permanentie - permanentiecentrum@ugent.be
     *     - Schoonmaak - schoonmaak@ugent.be
     *     - Veiligheid - veiligheid@ugent.be
     *
     * The mail is only sent if there are any locations that should be opened in 2 weeks from now().
     * There is a reason behind "2 weeks" from now and not "3 weeks". Every calendar period is locked for
     * updates by employees if the starts_at is less than 3 weeks from now. The mail we are sending
     * here is to notify the UGent services about all those locations that had been locked for updates
     * during last week. Therefore we need to get the overview of opening hours for locations 2 weeks
     * from now.
     *
     * For testing purposes, you can change the cron-value to "0 * * * * *" to trigger the scheduled task
     * every minute. Make sure to change the recipients as well to just include your own email address.
     * Extra note: if you want to test the mailing with the smtp.ugent.be mail server (as configured in
     * applications.properties), then you'll need to be connected with an UGent network. Either directly or
     * with VPN.
     */
    @Scheduled(cron = "0 0 6 * * MON")
    public void weeklyOpeningHoursMailing() {
        logger.info(String.format("Running scheduled task weeklyOpeningHoursMailing() with recipients %s",
                Arrays.toString(recipients)));
        try {
            LocalDate now = LocalDate.now();
            int year = now.getYear();
            int week = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);

            year = week > 50 ? year + 1 : year;
            week = week > 50 ? (week + 2) % 52 : week + 2;

            Map<String, String[]> openingHours = locationDao.getOpeningOverviewOfWeek(year, week);
            if (openingHours.size() > 0) {
                logger.info(String.format("Sending mail for scheduled tast weeklyOpeningHoursMailing() because in " +
                        "week %d of year %d, there are %d locations that have to be opened. Current week number is %d " +
                        "in year %d.", week, year, openingHours.size(), now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR),
                        now.getYear()));
                mailService.sendOpeningHoursOverviewMail(recipients, year, week);
            } else {
                logger.info(String.format("No mail is sent for scheduled task weeklyOpeningHoursMailing() because in " +
                        "week %d of year %d, there will be no locations opened. Current week number is %d in year %d.",
                        week, year, now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR), now.getYear()));
            }
        } catch (Exception e) {
            logger.error(String.format("The scheduled task weeklyOpeningHoursMailing() could " +
                    "not be executed due to an exception that was thrown: %s", e.getMessage()));
        }
    }

    /**
     * Scheduled task to be run every day at 21h00. This task fetches all unattended reservations for that day
     * and sends a mail to all unattended students (cfr. resources/templates/mail/not_attended.html for the mail).
     */
    @Scheduled(cron = "0 0 21 * * *")
    public void mailToUnattendedStudents() {
        try {
            List<Pair<LocationReservation, CalendarPeriod>> reservations =
                    locationReservationDao.getUnattendedLocationReservations(LocalDate.now());

            logger.info(String.format("Running scheduled task mailToUnattendedStudents() for %d reservations: %s",
                    reservations.size(), reservations));

            for (Pair<LocationReservation, CalendarPeriod> pair : reservations) {
                try {
                    mailService.sendMailToUnattendedStudent(pair.getFirst().getUser().getMail(),
                            pair.getFirst(), pair.getSecond());
                } catch (MessagingException e) {
                    logger.error(String.format("Could not send mail to unattended student for %s", pair));
                }
            }

        } catch (SQLException e) {
            logger.error(String.format("Could not fetch unattended location reservations: %s", e.getMessage()));
        }
    }

}