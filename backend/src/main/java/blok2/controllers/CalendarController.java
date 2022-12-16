package blok2.controllers;

import blok2.daos.ILocationDao;
import blok2.daos.ILocationReservationDao;
import blok2.daos.IUserDao;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

@RestController
@RequestMapping("/ical")
public class CalendarController {

    private final ILocationReservationDao locationReservationDao;
    private final ILocationDao locationDao;
    private final IUserDao userDao;

    public CalendarController(ILocationReservationDao locationReservationDao, ILocationDao locationDao, IUserDao userDao) {
        this.locationReservationDao = locationReservationDao;
        this.locationDao = locationDao;
        this.userDao = userDao;
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
