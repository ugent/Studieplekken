package blok2.controllers;

import blok2.daos.ILocationDao;
import blok2.daos.ILocationTagDao;
import blok2.helpers.authorization.AuthorizedLocationController;
import blok2.helpers.EmailService;
import blok2.helpers.LocationWithApproval;
import blok2.helpers.Resources;
import blok2.model.reservables.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This controller handles all requests related to locations.
 * Such as creating locations, list of locations, edit locations, ...
 */
@RestController
@RequestMapping("locations")
public class LocationController extends AuthorizedLocationController {

    private final Logger logger = LoggerFactory.getLogger(LocationController.class.getSimpleName());

    private final ILocationDao locationDao;
    private final ILocationTagDao locationTagDao;
    private final EmailService emailService;

    // *************************************
    // *   CRUD operations for LOCATIONS   *
    // *************************************

    @Autowired
    public LocationController(ILocationDao locationDao, ILocationTagDao locationTagDao, EmailService emailService) {
        this.locationDao = locationDao;
        this.locationTagDao = locationTagDao;
        this.emailService = emailService;
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

    @GetMapping("/unapproved")
    public List<Location> getAllUnapprovedLocations() {
        try {
            return locationDao.getAllUnapprovedLocations();
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
    public void addLocation(@RequestBody Location location) {
        isAuthorized((l,$) -> hasAuthority(l.getAuthority()), location);
        try {
            this.locationDao.addLocation(location);
            this.emailService.sendNewLocationMessage(Resources.blokatugentConf.getString("dfsgMail"), location);
            logger.info(String.format("New location %s added", location.getName()));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Mail error");
        }
    }

    @PutMapping("/{locationName}")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void updateLocation(@PathVariable("locationName") String locationName, @RequestBody Location location) {
        isAuthorized(locationName);
        try {
            // Get the location that is currently in db
            Location cl = locationDao.getLocation(locationName);

            // Make sure that only an admin could change the number of seats
            if (cl.getNumberOfSeats() != location.getNumberOfSeats() && !isAdmin())
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Changing seats can only be done by admins");

            locationDao.updateLocation(locationName, location);
            logger.info(String.format("Location %s updated", locationName));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping("/{locationName}/approval")
    public void approveLocation(@PathVariable("locationName") String locationName, @RequestBody LocationWithApproval landa) {
        try {
            locationDao.approveLocation(landa.getLocation(), landa.isApproval());
            logger.info(String.format("Location %s updated", locationName));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    //authority user
    //the updated location should be part of an authority the user is part of.
    @DeleteMapping("/{locationName}")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void deleteLocation(@PathVariable("locationName") String locationName) {
        isAuthorized(locationName);
        try {
            locationDao.deleteLocation(locationName);
            logger.info(String.format("Location %s deleted", locationName));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }


    /* currently no longer applicable
    //logged in user (?)
>>>>>>> master
    @GetMapping("/{locationName}/reservations/count")
    @PreAuthorize("permitAll()")
    public int getAmountOfReservationsToday(@PathVariable("locationName") String locationName) {
        try {
            return 0;//locationDao.getCountOfReservations(CustomDate.now()).get(locationName);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
    */

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
    public void setupTagsForLocation(@PathVariable("locationName") String locationName,
                                     @RequestBody List<Integer> tagIds) {
        isAuthorized(locationName);
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
