package blok2.controllers;

import blok2.daos.ILocationReservationDao;
import blok2.model.reservations.LocationReservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This controller handles all requests related to locationreservations.
 * Such as creating reservations, list of reservations, cancelling reservations,
 * scanning of users, ...
 */
@RestController
@RequestMapping("api/locations/reservations")
public class LocationReservationController {

    private final Logger logger = Logger.getLogger(LocationReservationController.class.getSimpleName());

    private final ILocationReservationDao locationReservationDao;

    @Autowired
    public LocationReservationController(ILocationReservationDao locationReservationDao) {
        this.locationReservationDao = locationReservationDao;
    }

    @GetMapping("/{userId}")
    public List<LocationReservation> getLocationReservationsByUserId(@PathVariable("userId") String userId) {
        try {
            return this.locationReservationDao.getAllLocationReservationsOfUser(userId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}
