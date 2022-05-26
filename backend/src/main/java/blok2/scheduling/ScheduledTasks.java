package blok2.scheduling;

import blok2.daos.ILocationDao;
import blok2.daos.ILocationReservationDao;
import blok2.daos.IUserDao;
import blok2.mail.MailReservationData;
import blok2.mail.MailService;
import blok2.model.reservables.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.IsoFields;
import java.util.*;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    private final ILocationReservationDao locationReservationDao;
    private final MailService mailService;
    private final ILocationDao locationDao;
    private final ReservationManager reservationManager;
    private final IUserDao userDao;

    private final String[] recipients;

    private static final int N_CONCURRENT_CONNECTIONS = 5;

    @Autowired
    public ScheduledTasks(ILocationReservationDao locationReservationDao, ILocationDao locationDao,
                          MailService mailService, Environment env, ReservationManager reservationManager, IUserDao userDao) {
        this.locationReservationDao = locationReservationDao;
        this.mailService = mailService;
        this.locationDao = locationDao;
        recipients = env.getProperty("custom.mailing.recipientsOpeningHoursOverview", String[].class);
        this.reservationManager = reservationManager;
        this.userDao = userDao;
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
        List<LocationReservation> reservations =
                locationReservationDao.getUnattendedLocationReservations(LocalDate.now());

        logger.info(String.format("Running scheduled task mailToUnattendedStudents() for %d recipients.",
                reservations.size()));

        // Send 5 mails concurrently n times. For the n+1-th time, send the remainder of the mails
        int n = reservations.size() / N_CONCURRENT_CONNECTIONS;
        for (int i = 0; i < n; i++) {
            mailToUnattendedStudentsInRange(reservations, i*N_CONCURRENT_CONNECTIONS,
                    i*N_CONCURRENT_CONNECTIONS + N_CONCURRENT_CONNECTIONS);
        }
        mailToUnattendedStudentsInRange(reservations, n*N_CONCURRENT_CONNECTIONS, reservations.size());
    }

    private void mailToUnattendedStudentsInRange(List<LocationReservation> reservations,
                                                 int start, int end) {
        Thread[] threads = new Thread[end - start];

        for (int i = start; i < end; i++) {
            LocationReservation lr = reservations.get(i);

            try {
                threads[i - start] = mailService.sendMailToUnattendedStudent(lr.getUser().getMail());
            } catch (MessagingException e) {
                logger.error(String.format("Could not send mail to unattended student for %s", lr));
            }
        }

        for (int i = 0; i < end - start; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                logger.error(String.format("Could not join on thread while sending mail to unattended student:" +
                        " %s", e.getMessage()));
            }
        }
    }

    /**
     * Scheduled task to be run every sunday at 10h00. This task fetches all students that have made at least one
     * reservation for next week and send them a reminder mail.
     */
    @Scheduled(cron = "0 0 10 * * SUN")
    public void mailReminderToStudentsWithReservation() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate tomorrowPlus7 = tomorrow.plusDays(7);

        List<User> users = locationReservationDao.getUsersWithReservationForWindowOfTime(tomorrow, tomorrowPlus7);
        logger.info(String.format("Running scheduled task mailReminderToStudentsWithReservation() for %d recipients.",
                users.size()));

        // Send 5 mails concurrently n times. For the n+1-th time, send the remainder of the mails
        int n = users.size() / N_CONCURRENT_CONNECTIONS;
        for (int i = 0; i < n; i++) {
            mailReminderToStudentsWithReservationInRange(users, i*N_CONCURRENT_CONNECTIONS,
                    i*N_CONCURRENT_CONNECTIONS + N_CONCURRENT_CONNECTIONS);
        }
        mailReminderToStudentsWithReservationInRange(users, n*N_CONCURRENT_CONNECTIONS, users.size());
    }

    private void mailReminderToStudentsWithReservationInRange(List<User> users, int start, int end) {
        Thread[] threads = new Thread[end - start];

        for (int i = start; i < end; i++) {
            User user = users.get(i);

            try {
                threads[i - start] = mailService.sendReminderToStudentsAboutReservation(user.getMail());
            } catch (MessagingException e) {
                logger.error(String.format("Could not send reminder mail to student for %s", user));
            }
        }

        for (int i = 0; i < end - start; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                logger.error(String.format("Could not join on thread while sending a reminder mail to student:" +
                        " %s", e.getMessage()));
            }
        }
    }

    /**
     * Scheduled task to be run every day at 21h30. This task fetches all made reservations at that day
     * and sends a mail to all those students (cfr. resources/templates/mail/reservation_confirmation_past24hrs.html for the mail).
     */
    @Scheduled(cron = "0 30 21 * * *")
    public void sendReservationsPast24hrs() {
        // Gather all reservations from past 24hrs and group them by user.
        Map<User, List<MailReservationData>> usermap = new HashMap<>();
        Map<Integer, Location> locationMap = new HashMap<>();
        List<LocationReservation> newReservations = this.locationReservationDao.getReservationCreatedToday();
        for (LocationReservation reservation : newReservations) {
            locationMap.computeIfAbsent(reservation.getTimeslot().getLocationId(), (locationDao::getLocationById));

            Location location = locationMap.get(reservation.getTimeslot().getLocationId());
            String locationName = location.getName();
            LocalDateTime time = reservation.getTimeslot().timeslotDate().atTime(reservation.getTimeslot().getOpeningHour());
            String locationReminderDutch = location.getReminderDutch().trim();
            String locationReminderEnglish = location.getReminderEnglish().trim();

            usermap.computeIfAbsent(reservation.getUser(), (user -> new ArrayList<>()))
                .add(new MailReservationData(locationName, time, locationReminderDutch, locationReminderEnglish));
        }
        // Send mails to the users about their reservations N_CONCURRENT_CONNECTIONS at a time.
        Thread[] threads = new Thread[N_CONCURRENT_CONNECTIONS];
        int currentConnections = 0;
        for (User user : usermap.keySet()) {
            if (!user.getUserSettings().isReceiveMailConfirmation()) {
                continue; // Skip if user doesn't want mail confirmations of reservations.
            }
            try {
                threads[currentConnections] = mailService.sendMailConfirmingLast24hrsOfReservations(user.getMail(), usermap.get(user));
            } catch (MessagingException e) {
                logger.error(String.format("Could not join on thread while sending mail to unattended student: %s", e.getMessage()));
            }
            currentConnections += 1;
            if (currentConnections == N_CONCURRENT_CONNECTIONS) {
                for (int i = 0; i < N_CONCURRENT_CONNECTIONS; i += 1) {
                    try {
                        threads[i].join();
                    } catch (InterruptedException e) {
                        logger.error(String.format("Could not join on thread while sending mail to unattended student: %s", e.getMessage()));
                    }
                }
                currentConnections = 0;
            }
        }
        for (int i = 0; i < currentConnections; i += 1) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                logger.error(String.format("Could not join on thread while sending mail to unattended student: %s", e.getMessage()));
            }
        }
    }

    /**
     * Schedules the 'Random' reservation pools to be processed every minute.
     */
    @Scheduled(cron = "0 * * * * *")
    public void scheduleRandomPools() {
        reservationManager.scheduleRandomPools();
    }
    

}
