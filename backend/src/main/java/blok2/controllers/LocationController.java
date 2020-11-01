package blok2.controllers;

import blok2.daos.ILocationDao;
import blok2.daos.ILocationTagDao;
import blok2.helpers.date.CustomDate;
import blok2.model.reservables.Location;
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
import java.util.stream.Collectors;

/**
 * This controller handles all requests related to locations.
 * Such as creating locations, list of locations, edit locations, ...
 */
@RestController
@RequestMapping("api/locations")
public class LocationController {

    private final Logger logger = LoggerFactory.getLogger(LocationController.class.getSimpleName());

    private final ILocationDao locationDao;
    private final ILocationTagDao locationTagDao;

    // *************************************
    // *   CRUD operations for LOCATIONS   *
    // *************************************

    @Autowired
    public LocationController(ILocationDao locationDao, ILocationTagDao locationTagDao) {
        this.locationDao = locationDao;
        this.locationTagDao = locationTagDao;
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Location> getAllLocations() {
        try {
            return locationDao.getAllLocations();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/{locationName}")
    @PreAuthorize("permitAll()")
    public Location getLocation(@PathVariable("locationName") String locationName) {
        try {
            return locationDao.getLocation(locationName);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    // TODO: if only 'HAS_AUTHORITIES', then adding a location is only allowed within one of the user's authorities
    public void addLocation(@RequestBody Location location) {
        try {
            this.locationDao.addLocation(location);
            logger.info(String.format("New location %s added", location.getName()));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping("/{locationName}")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    // TODO: if only 'HAS_AUTHORITIES', then updating a location is only allowed within one of the user's authorities
    public void updateLocation(@PathVariable("locationName") String locationName, @RequestBody Location location) {
        try {
            locationDao.updateLocation(locationName, location);
            logger.info(String.format("Location %s updated", locationName));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @DeleteMapping("/{locationName}")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    // TODO: if only 'HAS_AUTHORITIES', then deleting a location is only allowed within one of the user's authorities
    public void deleteLocation(@PathVariable("locationName") String locationName) {
        try {
            locationDao.deleteLocation(locationName);
            logger.info(String.format("Location %s deleted", locationName));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/{locationName}/reservations/count")
    @PreAuthorize("permitAll()")
    public int getAmountOfReservationsToday(@PathVariable("locationName") String locationName) {
        try {
            return locationDao.getCountOfReservations(CustomDate.now()).get(locationName);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    // *****************************************
    // *   CRUD operations for LOCATION_TAGS   *
    // *****************************************

    /**
     * Following endpoint is a one-fits-all method: all tags that are supposed
     * to be set, must be provided in the body. Upon success only the tags
     * that have been provided, will be set for the location
     */
    @PutMapping("/tags/{locationName}")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    // TODO: if only 'HAS_AUTHORITIES', then setting up tags is only allowed for a location within one of the user's authorities
    public void setupTagsForLocation(@PathVariable("locationName") String locationName,
                                     @RequestBody List<Integer> tagIds) {
        try {
            logger.info(String.format("Setting up the tags for location '%s' with ids [%s]",
                    locationName, tagIds.stream().map(String::valueOf).collect(Collectors.joining(", "))));
            locationTagDao.deleteAllTagsFromLocation(locationName);
            locationTagDao.bulkAddTagsToLocation(locationName, tagIds);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}
