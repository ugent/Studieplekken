package be.ugent.blok2.controllers;

import be.ugent.blok2.daos.ILocationDao;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.model.reservables.Location;
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
 * This controller handles all requests related to locations.
 * Such as creating locations, list of locations, edit locations, ...
 */
@RestController
@RequestMapping("api/locations")
public class LocationController {

    private final Logger logger = Logger.getLogger(LocationController.class.getSimpleName());

    private final ILocationDao locationDao;

    @Autowired
    public LocationController(ILocationDao locationDao) {
        this.locationDao = locationDao;
    }

    //logged in user
    @GetMapping
    public List<Location> getAllLocations() {
        try {
            return locationDao.getAllLocations();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    //logged in user
    @GetMapping("/{locationName}")
    public Location getLocation(@PathVariable("locationName") String locationName) {
        try {
            return locationDao.getLocation(locationName);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    //authority user
    // location should be part of an authority the user is part of.
    @PostMapping
    public void addLocation(@RequestBody Location location) {
        try {
            this.locationDao.addLocation(location);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    //authority user
    //the updated location should be part of an authority the user is part of.
    @PutMapping("/{locationName}")
    public void updateLocation(@PathVariable("locationName") String locationName, @RequestBody Location location) {
        try {
            locationDao.updateLocation(locationName, location);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    //authority user
    //the updated location should be part of an authority the user is part of.
    @DeleteMapping("/{locationName}")
    public void deleteLocation(@PathVariable("locationName") String locationName) {
        try {
            locationDao.deleteLocation(locationName);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    //logged in user (?)
    @GetMapping("/{locationName}/reservations/count")
    public int getAmountOfReservationsToday(@PathVariable("locationName") String locationName) {
        try {
            return locationDao.getCountOfReservations(CustomDate.now()).get(locationName);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}
