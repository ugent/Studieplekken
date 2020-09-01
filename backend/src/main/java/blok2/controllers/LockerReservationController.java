package blok2.controllers;

import blok2.daos.ILockerReservationDao;
import blok2.model.reservations.LockerReservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This controller handles all requests related to lockerreservations.
 * Such as creating reservations, list of reservations, cancelling reservations, ...
 */
@RestController
@RequestMapping("api/lockers/reservations")
public class LockerReservationController {

    private final Logger logger = Logger.getLogger(LockersController.class.getSimpleName());

    private final ILockerReservationDao lockerReservationDao;

    @Autowired
    public LockerReservationController(ILockerReservationDao lockerReservationDao) {
        this.lockerReservationDao = lockerReservationDao;
    }

    @GetMapping("/{userId}")
    public List<LockerReservation> getLockerReservationsOfUserById(@PathVariable("userId") String userId) {
        try {
            return lockerReservationDao.getAllLockerReservationsOfUser(userId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping
    public void updateLockerReservation(@RequestBody LockerReservation lockerReservation) {
        try {
            lockerReservationDao.changeLockerReservation(lockerReservation);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}
