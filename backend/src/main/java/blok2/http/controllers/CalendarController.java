package blok2.http.controllers;

import blok2.database.dao.*;
import blok2.model.calendar.Timeslot;
import blok2.model.location.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.*;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static blok2.scheduling.PoolProcessor.RANDOM_RESERVATION_DURATION_MINS;

@RestController
@RequestMapping("/ical")
public class CalendarController {

    private final ILocationReservationDao locationReservationDao;
    private final ILocationDao locationDao;
    private final IUserDao userDao;
    private final IUserLocationSubscriptionDao userLocationSubscriptionDao;
    private final ITimeslotDao timeslotDao;

    public CalendarController(ILocationReservationDao locationReservationDao, ILocationDao locationDao, IUserDao userDao,
                              IUserLocationSubscriptionDao userLocationSubscriptionDao, ITimeslotDao timeslotDao) {
        this.locationReservationDao = locationReservationDao;
        this.locationDao = locationDao;
        this.userDao = userDao;
        this.userLocationSubscriptionDao = userLocationSubscriptionDao;
        this.timeslotDao = timeslotDao;
    }

    @GetMapping("/{userId}/{calendarId}")
    public ResponseEntity getCalender(@PathVariable("userId") String userId, @PathVariable("calendarId") String calendarId) {

        User user = userDao.getUserById(userId);
        if (user == null || !user.getCalendarId().toString().equals(calendarId)) {
            return ResponseEntity.badRequest().body("Calendar not found");
        }

        List<LocationReservation> reservations = locationReservationDao.getAllLocationReservationsOfUser(userId);

        // Create the calendar
        Calendar calendar = new Calendar();
        calendar.add(new ProdId("-//Studieplekken Calendar//iCal4j 1.0//EN"));
        Version version = new Version();
        version.setValue(Version.VALUE_2_0);
        calendar.add(version);
        calendar.add(new CalScale(CalScale.VALUE_GREGORIAN));

        // Add the events to the calendar
        for (LocationReservation reservation : reservations) {
            // Location reservation start and end time in milliseconds
            long startDateTimeInMillis = LocalDateTime.of(reservation.getTimeslot().timeslotDate(), reservation.getTimeslot().getOpeningHour()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endDateTimeInMillis = LocalDateTime.of(reservation.getTimeslot().timeslotDate(), reservation.getTimeslot().getClosingHour()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            java.util.Calendar calendarStartTime = new GregorianCalendar();
            calendarStartTime.setTimeInMillis(startDateTimeInMillis);

            // Timezone info
            TimeZone tz = calendarStartTime.getTimeZone();
            ZoneId zid = tz.toZoneId();

            // Generate unique identifier
            UidGenerator ug = new RandomUidGenerator();
            Uid uid = ug.generateUid();

            // Create the calendar event
            LocalDateTime start = LocalDateTime.ofInstant(calendarStartTime.toInstant(), zid);
            LocalDateTime end = LocalDateTime.ofInstant(Instant.ofEpochMilli(endDateTimeInMillis), zid);
            String summary = locationDao.getLocationById(reservation.getTimeslot().getLocationId()).getName();
            VEvent event = new VEvent(start, end, summary);
            event.add(uid);

            // Add the event to the calendar
            calendar.add(event);
        }

        // Add the location reservation openings for which the user subscribed to the calendar
        List<Location> subscribedLocations = userLocationSubscriptionDao.getSubscribedLocations(user);
        for (Location location : subscribedLocations) {
            // Get all timeslots for the location
            List<LocalDateTime> addedTimes = new ArrayList<>();
            List<Timeslot> timeslots = timeslotDao.getTimeslotsOfLocationAfterTimeslotDate(location.getLocationId(), LocalDate.now().minusWeeks(1)).stream().filter(Timeslot::isReservable).collect(Collectors.toList());
            for (Timeslot timeslot : timeslots) {
                if (addedTimes.contains(timeslot.getReservableFrom())) {
                    continue;
                }
                addedTimes.add(timeslot.getReservableFrom());

                // Location reservation start and end time in milliseconds
                long startDateTimeInMillis = timeslot.getReservableFrom().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long endDateTimeInMillis = timeslot.getReservableFrom().plusMinutes(RANDOM_RESERVATION_DURATION_MINS).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

                java.util.Calendar calendarStartTime = new GregorianCalendar();
                calendarStartTime.setTimeInMillis(startDateTimeInMillis);

                // Timezone info
                TimeZone tz = calendarStartTime.getTimeZone();
                ZoneId zid = tz.toZoneId();

                // Generate unique identifier
                UidGenerator ug = new RandomUidGenerator();
                Uid uid = ug.generateUid();

                // Create the calendar event
                LocalDateTime start = LocalDateTime.ofInstant(calendarStartTime.toInstant(), zid);
                LocalDateTime end = LocalDateTime.ofInstant(Instant.ofEpochMilli(endDateTimeInMillis), zid);
                String summary = location.getName();
                VEvent event = new VEvent(start, end, summary);
                event.add(uid);

                // Add notification
                VAlarm reminder = new VAlarm(Duration.ofMinutes(-10));
                reminder.add(new Action(Action.VALUE_DISPLAY));
                reminder.add(new Description(summary));
                event.add(reminder);

                // Add the event to the calendar
                calendar.add(event);
            }
        }

        byte[] calendarBytes = calendar.toString().getBytes();
        Resource resource = new ByteArrayResource(calendarBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=calendar.ics");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }
}
