package blok2.controllers;

import blok2.daos.ILocationReservationDao;
import blok2.model.reservations.LocationReservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

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
    @PreAuthorize("(hasAuthority('USER') and #id == authentication.principal.augentID) or " +
                  "hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    // TODO: if only 'HAS_AUTHORITIES', then only allowed to retrieve the reservations for a location within one of the user's authorities
    public List<LocationReservation> getLocationReservationsByUserId(@RequestParam String id) {
        try {
            return locationReservationDao.getAllLocationReservationsOfUser(id);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/location")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    // TODO: if only 'HAS_AUTHORITIES', then only allowed to retrieve the reservations for a location within one of the user's authorities
    public List<LocationReservation> getLocationReservationsOfLocation(@RequestParam String locationName,
                                                                       @RequestParam boolean pastReservations) {
        try {
            return locationReservationDao.getAllLocationReservationsOfLocation(locationName, pastReservations);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/from")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    // TODO: if only 'HAS_AUTHORITIES', then only allowed to retrieve the reservations for a location within one of the user's authorities
    public List<LocationReservation> getLocationReservationsOfLocationFrom(@RequestParam String locationName,
                                                                           @RequestParam String start,
                                                                           @RequestParam boolean pastReservations) {
        try {
            return locationReservationDao.getAllLocationReservationsOfLocationFrom(locationName, start, pastReservations);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/until")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    // TODO: if only 'HAS_AUTHORITIES', then only allowed to retrieve the reservations for a location within one of the user's authorities
    public List<LocationReservation> getLocationReservationsOfLocationUntil(@RequestParam String locationName,
                                                                            @RequestParam String end,
                                                                            @RequestParam boolean pastReservations) {
        try {
            return locationReservationDao.getAllLocationReservationsOfLocationUntil(locationName, end, pastReservations);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/fromAndUntil")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    // TODO: if only 'HAS_AUTHORITIES', then only allowed to retrieve the reservations for a location within one of the user's authorities
    public List<LocationReservation> getLocationReservationsOfLocationFromAndUntil(@RequestParam String locationName,
                                                                                   @RequestParam String start,
                                                                                   @RequestParam String end,
                                                                                   @RequestParam boolean pastReservations) {
        try {
            return locationReservationDao
                    .getAllLocationReservationsOfLocationFromAndUntil(locationName, start, end, pastReservations);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    // TODO: if only 'HAS_AUTHORITIES', then only allowed to delete a reservation for a location within one of the user's authorities
    //       if only 'USER', then only allowed to delete a reservation that is made by the user
    public void deleteLocationReservation(@RequestBody LocationReservation locationReservation) {
        try {
            locationReservationDao.deleteLocationReservation(locationReservation.getUser().getAugentID(),
                    locationReservation.getDate());
            logger.info(String.format("LocationReservation for user %s at time %s deleted", locationReservation.getUser(), locationReservation.getDate().toString()));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}
