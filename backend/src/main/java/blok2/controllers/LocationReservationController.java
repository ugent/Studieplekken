package blok2.controllers;

import blok2.daos.ILocationReservationDao;
import blok2.helpers.date.CustomDate;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * This controller handles all requests related to location reservations.
 * Such as creating reservations, list of reservations, cancelling reservations,
 * scanning of users, ...
 */
@RestController
@RequestMapping("api/locations/reservations")
public class LocationReservationController {

    private final Logger logger = LoggerFactory.getLogger(LocationReservationController.class.getSimpleName());

    private final ILocationReservationDao locationReservationDao;

    @Autowired
    public LocationReservationController(ILocationReservationDao locationReservationDao) {
        this.locationReservationDao = locationReservationDao;
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

    @PostMapping("/new")
    public LocationReservation createLocationReservation(@AuthenticationPrincipal User user, @RequestBody @Valid Timeslot timeslot) {
        try {
            LocationReservation reservation = new LocationReservation(user, CustomDate.today().toDateString(), timeslot, null);
            if(!locationReservationDao.addLocationReservationIfStillRoomAtomically(reservation)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "There are no more spots left for this location.");
            }
            return locationReservationDao.getLocationReservation(user.getAugentID(), timeslot);
        }catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/timeslot")
    public List<LocationReservation> getLocationReservationsByTimeslot(@RequestParam @Valid Timeslot timeslot) {
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
}
