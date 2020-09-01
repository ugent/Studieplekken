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
 * This controller handles all requests related to location reservations.
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

    @GetMapping("/user")
    public List<LocationReservation> getLocationReservationsByUserId(@RequestParam String id) {
        try {
            return locationReservationDao.getAllLocationReservationsOfUser(id);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/location")
    public List<LocationReservation> getLocationReservationsOfLocation(@RequestParam String locationName,
                                                                       @RequestParam boolean pastReservations) {
        try {
            return locationReservationDao.getAllLocationReservationsOfLocation(locationName, pastReservations);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/from")
    public List<LocationReservation> getLocationReservationsOfLocationFrom(@RequestParam String locationName,
                                                                           @RequestParam String start,
                                                                           @RequestParam boolean pastReservations) {
        try {
            return locationReservationDao.getAllLocationReservationsOfLocationFrom(locationName, start, pastReservations);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/until")
    public List<LocationReservation> getLocationReservationsOfLocationUntil(@RequestParam String locationName,
                                                                            @RequestParam String end,
                                                                            @RequestParam boolean pastReservations) {
        try {
            return locationReservationDao.getAllLocationReservationsOfLocationUntil(locationName, end, pastReservations);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/fromAndUntil")
    public List<LocationReservation> getLocationReservationsOfLocationFromAndUntil(@RequestParam String locationName,
                                                                            @RequestParam String start,
                                                                            @RequestParam String end,
                                                                            @RequestParam boolean pastReservations) {
        try {
            return locationReservationDao
                    .getAllLocationReservationsOfLocationFromAndUntil(locationName, start, end, pastReservations);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @DeleteMapping
    public void deleteLocationReservation(@RequestBody LocationReservation locationReservation) {
        try {
            locationReservationDao.deleteLocationReservation(locationReservation.getUser().getAugentID(),
                    locationReservation.getDate());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}
