package blok2.controllers;

import blok2.daos.ICalendarPeriodDao;
import blok2.daos.ILocationReservationDao;
import blok2.helpers.Pair;
import blok2.helpers.authorization.AuthorizedLocationController;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
import blok2.mail.MailService;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * This controller handles all requests related to location reservations.
 * Such as creating reservations, list of reservations, cancelling reservations,
 * scanning of users, ...
 */
@RestController
@RequestMapping("locations/reservations")
public class LocationReservationController extends AuthorizedLocationController {

    private final Logger logger = LoggerFactory.getLogger(LocationReservationController.class.getSimpleName());

    private final ILocationReservationDao locationReservationDao;
    private final ICalendarPeriodDao calendarPeriodDao;

    private final MailService mailService;

    @Autowired
    public LocationReservationController(ILocationReservationDao locationReservationDao, ICalendarPeriodDao calendarPeriodDao, MailService mailService) {
        this.locationReservationDao = locationReservationDao;
        this.calendarPeriodDao = calendarPeriodDao;
        this.mailService = mailService;
    }

    @GetMapping("/user")
    @PreAuthorize("(hasAuthority('USER') and #id == authentication.principal.userId) or hasAuthority('ADMIN')")
    // TODO: if only 'HAS_AUTHORITIES', then only allowed to retrieve the reservations for a location within one of the user's authorities
    // Not sure why you'd be allowed to get a user's reservations if you own a location.
    // TODO: We suddenly use a request parameter here. Probably better to streamline it with everything else and put it in the url.
    public List<LocationReservation> getLocationReservationsByUserId(@RequestParam String id) {
        return locationReservationDao.getAllLocationReservationsOfUser(id);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER') and #userId == authentication.principal.userId")
    public List<Pair<LocationReservation, CalendarPeriod>>
    getLocationReservationsWithLocationByUserId(@PathVariable("userId") String userId) {
        return locationReservationDao.getAllLocationReservationsAndCalendarPeriodsOfUser(userId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public LocationReservation createLocationReservation(@AuthenticationPrincipal User user, @Valid @RequestBody Timeslot timeslot) {
        try {
            LocationReservation reservation = new LocationReservation(user, timeslot, null);
            CalendarPeriod period = calendarPeriodDao.getById(timeslot.getCalendarId());
            if (LocalDateTime.now().isBefore(period.getReservableFrom())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This calendarperiod can't yet be reserved");
            }
            if (!locationReservationDao.addLocationReservationIfStillRoomAtomically(reservation)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "There are no more spots left for this location.");
            }
            return locationReservationDao.getLocationReservation(user.getUserId(), timeslot);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/timeslot/{calendarid}/{date}/{seqnr}")
    @PreAuthorize("hasAuthority('HAS_VOLUNTEERS') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<LocationReservation> getLocationReservationsByTimeslot(
            @PathVariable("calendarid") int calendarId,
            @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @PathVariable("seqnr") int seqnr
    ) {
        Timeslot timeslot = new Timeslot(calendarId, seqnr, date, 0);
        return locationReservationDao.getAllLocationReservationsOfTimeslot(timeslot);
    }

    @GetMapping("/count/{locationId}")
    @PreAuthorize("permitAll()")
    public Map<String, Integer> getReservationCount(@PathVariable("locationId") int locationId) {
        return Collections.singletonMap("amount", locationReservationDao.amountOfReservationsRightNow(locationId));
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void deleteLocationReservation(@AuthenticationPrincipal User user, @RequestBody @Valid LocationReservation locationReservation) {

        try {
            CalendarPeriod parentPeriod = calendarPeriodDao.getById(locationReservation.getTimeslot().getCalendarId());
            isAuthorized(
                    (lr, u) -> hasAuthority(parentPeriod.getLocation().getLocationId()) || lr.getUser().getUserId().equals(u.getUserId()),
                    locationReservation
            );

            locationReservationDao.deleteLocationReservation(locationReservation.getUser().getUserId(),
                    locationReservation.getTimeslot());
            logger.info(String.format("LocationReservation for user %s at time %s deleted", locationReservation.getUser(), locationReservation.getTimeslot().toString()));

            // Send email to student if student is not the user who requested the deletion.
            if (!locationReservation.getUser().getUserId().equals(user.getUserId())) {
                try {
                    mailService.sendReservationSlotDeletedMessage(locationReservation.getUser().getMail(), locationReservation.getTimeslot());
                } catch (MessagingException e) {
                    logger.error(String.format("Could not send mail to student %s about deleted reservation slot %s", user.getUsername(), locationReservation.getTimeslot().toString()));
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PostMapping("/{userid}/{calendarid}/{date}/{seqnr}/attendance")
    @PreAuthorize("hasAuthority('HAS_VOLUNTEERS') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void setLocationReservationAttendance(
            @PathVariable("calendarid") int calendarId,
            @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @PathVariable("seqnr") int seqnr,
            @PathVariable("userid") String userid,
            @RequestBody LocationReservation.AttendedPostBody body
    ) {
        Timeslot slot = new Timeslot(calendarId, seqnr, date, 0);
        try {
            CalendarPeriod parentPeriod = calendarPeriodDao.getById(slot.getCalendarId());
            isVolunteer(parentPeriod.getLocation());
            if (!locationReservationDao.setReservationAttendance(userid, slot, body.getAttended()))
                throw new NoSuchDatabaseObjectException("No such reservation");
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping("/not-scanned")
    @PreAuthorize("hasAuthority('HAS_VOLUNTEERS') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void setAllNotScannedStudentsToUnattendedForTimeslot(@RequestBody Timeslot timeslot) {
        try {
            // check if user is allowed by role
            CalendarPeriod calendarPeriod = calendarPeriodDao.getById(timeslot.getCalendarId());
            isVolunteer(calendarPeriod.getLocation());

            logger.info(String.format("Setting all students who were not scanned to unattended for timeslot %s", timeslot));
            locationReservationDao.setNotScannedStudentsToUnattended(timeslot);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

}
