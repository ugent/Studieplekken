package blok2.controllers;

import blok2.daos.ILockerReservationDao;
import blok2.model.reservations.LockerReservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * This controller handles all requests related to locker reservations.
 * Such as creating reservations, list of reservations, cancelling reservations, ...
 */
@RestController
@RequestMapping("lockers/reservations")
public class LockerReservationController {

    private final Logger logger = LoggerFactory.getLogger(LockerReservation.class.getSimpleName());

    private final ILockerReservationDao lockerReservationDao;

    @Autowired
    public LockerReservationController(ILockerReservationDao lockerReservationDao) {
        this.lockerReservationDao = lockerReservationDao;
    }

    @GetMapping("/user")
    public List<LockerReservation> getLockerReservationsOfUserById(@RequestParam String id) {
        try {
            return lockerReservationDao.getAllLockerReservationsOfUser(id);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/location")
    public List<LockerReservation> getLocationReservationsOfLocation(@RequestParam String locationName,
                                                                     @RequestParam boolean pastReservations) {
        try {
            return lockerReservationDao.getAllLockerReservationsOfLocation(locationName, pastReservations);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping
    public void updateLockerReservation(@RequestBody LockerReservation lockerReservation) {
        try {
            lockerReservationDao.changeLockerReservation(lockerReservation);
            logger.info(String.format("Updating locker reservation by owner %s for locker %d in location %s", lockerReservation.getOwner(), lockerReservation.getLocker().getNumber(), lockerReservation.getLocker().getLocation().getName()));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @DeleteMapping
    public void deleteLockerReservation(@RequestBody LockerReservation lockerReservation) {
        try {
            lockerReservationDao.deleteLockerReservation(lockerReservation.getLocker().getLocation().getName(),
                    lockerReservation.getLocker().getNumber());
            logger.info(String.format("Deleting locker reservation by owner %s for locker %d in location %s", lockerReservation.getOwner(), lockerReservation.getLocker().getNumber(), lockerReservation.getLocker().getLocation().getName()));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}
