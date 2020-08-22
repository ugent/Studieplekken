package be.ugent.blok2.controllers;

import be.ugent.blok2.daos.ILocationReservationDao;
import be.ugent.blok2.model.reservations.LocationReservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.*;

/**
 * This controller handles all requests related to locationreservations.
 * Such as creating reservations, list of reservations, cancelling reservations,
 * scanning of users, ...
 */
@RestController
@RequestMapping("api/locations/reservations")
public class LocationReservationController {
    private final ILocationReservationDao locationReservationDao;

    @Autowired
    public LocationReservationController(ILocationReservationDao locationReservationDao) {
        this.locationReservationDao = locationReservationDao;
    }

    @GetMapping("/{userId}")
    public List<LocationReservation> getLocationReservationsByUserId(@PathVariable("userId") String userId) {
        try {
            return this.locationReservationDao.getAllLocationReservationsOfUser(userId);
        } catch (SQLException ignore) {
            return null;
        }
    }
}
