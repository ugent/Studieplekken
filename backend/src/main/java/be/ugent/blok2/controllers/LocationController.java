package be.ugent.blok2.controllers;

import be.ugent.blok2.daos.ILocationDao;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.model.reservables.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

/**
 * This controller handles all requests related to locations.
 * Such as creating locations, list of locations, edit locations, ...
 */
@RestController
@RequestMapping("api/locations")
public class LocationController {

    private final ILocationDao locationDao;

    @Autowired
    public LocationController(ILocationDao locationDao) {
        this.locationDao = locationDao;
    }

    @GetMapping
    public List<Location> getAllLocations() {
        try {
            return locationDao.getAllLocations();
        } catch (SQLException ignore) {
            return null;
        }
    }

    @GetMapping("/{locationName}")
    public Location getLocation(@PathVariable("locationName") String locationName) {
        try {
            return locationDao.getLocation(locationName);
        } catch (SQLException ignore) {
            return null;
        }
    }

    @GetMapping("/{locationName}/reservations/count")
    public int getAmountOfReservationsToday(@PathVariable("locationName") String locationName) {
        try {
            return locationDao.getCountOfReservations(CustomDate.now()).get(locationName);
        } catch (SQLException ignore) {
            return 0;
        }
    }
}

