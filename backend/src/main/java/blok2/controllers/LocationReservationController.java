package blok2.controllers;

import blok2.daos.ICalendarPeriodDao;
import blok2.daos.ILocationReservationDao;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * This controller handles all requests related to location reservations.
 * Such as creating reservations, list of reservations, cancelling reservations,
 * scanning of users, ...
 */
@RestController
@RequestMapping("locations/reservations")
public class LocationReservationController {

    private final Logger logger = LoggerFactory.getLogger(LocationReservationController.class.getSimpleName());

    private final ILocationReservationDao locationReservationDao;
    private final ICalendarPeriodDao calendarPeriodDao;

    // @Autowired
    // SmartValidator validator;

    @Autowired
    public LocationReservationController(ILocationReservationDao locationReservationDao, ICalendarPeriodDao calendarPeriodDao) {
        this.locationReservationDao = locationReservationDao;
        this.calendarPeriodDao = calendarPeriodDao;
    }

    @GetMapping("/user")
    public List<LocationReservation> getLocationReservationsByUserId(@RequestParam String id) {
        try {
            return locationReservationDao.getAllLocationReservationsOfUser(id);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PostMapping
    public LocationReservation createLocationReservation(@AuthenticationPrincipal User user, @Valid @RequestBody Timeslot timeslot) {
        try {
            LocationReservation reservation = new LocationReservation(user, LocalDateTime.now(), timeslot, null);
            CalendarPeriod period = calendarPeriodDao.getById(timeslot.getCalendarId());
            if(LocalDateTime.now().isBefore(period.getReservableFrom())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This calendarperiod can't yet be reserved");
            }
            if(!locationReservationDao.addLocationReservationIfStillRoomAtomically(reservation)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "There are no more spots left for this location.");
            }
            return locationReservationDao.getLocationReservation(user.getAugentID(), timeslot);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/timeslot/{calendarid}/{date}/{seqnr}")
    public List<LocationReservation> getLocationReservationsByTimeslot(@PathVariable("calendarid") int calendarId, @PathVariable("date") @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate date, @PathVariable("seqnr") int seqnr) {
        Timeslot timeslot = new Timeslot(calendarId, seqnr, date);
        try {
            return locationReservationDao.getAllLocationReservationsOfTimeslot(timeslot);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @DeleteMapping
    public void deleteLocationReservation(@RequestBody @Valid LocationReservation locationReservation) {
        try {
            locationReservationDao.deleteLocationReservation(locationReservation.getUser().getAugentID(),
                    locationReservation.getTimeslot());
            logger.info(String.format("LocationReservation for user %s at time %s deleted", locationReservation.getUser(), locationReservation.getTimeslot().toString()));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PostMapping("/{userid}/{calendarid}/{date}/{seqnr}/attendance")
    public void setLocationReservationAttendance(@PathVariable("calendarid") int calendarId, @PathVariable("date") @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate date,
                                       @PathVariable("seqnr") int seqnr, @PathVariable("userid") String userid, @RequestBody LocationReservation.AttendedPostBody body) {
        Timeslot slot = new Timeslot(calendarId, seqnr, date);
        try {
            locationReservationDao.setReservationAttendance(userid, slot, body.getAttended());
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}
