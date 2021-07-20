package blok2.controllers;

import blok2.daos.*;
import blok2.helpers.LocationWithApproval;
import blok2.helpers.authorization.AuthorizedLocationController;
import blok2.helpers.exceptions.AlreadyExistsException;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
import blok2.helpers.exceptions.NotAuthorizedException;
import blok2.helpers.orm.LocationNameAndNextReservableFrom;
import blok2.mail.MailService;
import blok2.model.reservables.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.MessagingException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    private final IUserDao userDao;
    private final IVolunteerDao volunteerDao;
    private final ILocationReservationDao locationReservationDao;

    private final MailService mailService;

    // *************************************
    // *   CRUD operations for LOCATIONS   *
    // *************************************

    @Autowired
    public LocationController(ILocationDao locationDao, ILocationTagDao locationTagDao, IUserDao userDao,
                              IVolunteerDao volunteerDao, MailService mailService, ILocationReservationDao locationReservationDao) {
        this.locationDao = locationDao;
        this.locationTagDao = locationTagDao;
        this.userDao = userDao;
        this.mailService = mailService;
        this.volunteerDao = volunteerDao;
        this.locationReservationDao = locationReservationDao;
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Location> getAllLocations() {
        List<Location> locations = locationDao.getAllActiveLocations();
        locations.sort(Comparator.comparing(Location::getName));
        return locations;
    }

    @GetMapping("/unapproved")
    public List<Location> getAllUnapprovedLocations() {
        return locationDao.getAllUnapprovedLocations();
    }

    @GetMapping("/{locationId}")
    @PreAuthorize("permitAll()")
    public Location getLocation(@PathVariable("locationId") int locationId) {
        return locationDao.getLocationById(locationId);
    }

    @GetMapping("/nextReservableFroms")
    @PreAuthorize("permitAll()")
    public List<LocationNameAndNextReservableFrom> getAllNextReservableFroms() {
        return locationDao.getNextReservationMomentsOfAllLocations();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void addLocation(@AuthenticationPrincipal User user, @RequestBody Location location) {
        isAuthorized((l, $) -> hasAuthority(l.getAuthority()), location);
        try {
            try {
                locationDao.getLocationByName(location.getName());
                throw new AlreadyExistsException("location name already in use");
            } catch (NoSuchDatabaseObjectException ignore) {
                // good to go
            }

            // Check if the user is an admin or is in the same institution that he is adding a location to.
            if (!user.isAdmin() && !user.getInstitution().equals(location.getBuilding().getInstitution())) {
                throw new NotAuthorizedException("You are not authorized to add a new location for this institution.");
            }

            this.locationDao.addLocation(location);

            // Send a mail to the admins to notify them about the creation of a new location.
            // Note: this mail is not sent in development or while testing (see implementation,
            // of the sendMail() methods in MailServer)
            String[] admins = userDao.getAdmins().stream().map(User::getMail).toArray(String[]::new);
            logger.info(String.format("Sending mail to admins to notify about creation of new location %s. Recipients are: %s", location, Arrays.toString(admins)));
            mailService.sendNewLocationMessage(admins, location);

            logger.info(String.format("New location %s added", location.getName()));
        } catch (MessagingException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Mailing error");
        }
    }

    @PutMapping("/{locationId}")
    @PreAuthorize("@authorizedInstitutionController.hasAuthorityLocation(authentication.principal, #locationId)")
    public void updateLocation(@AuthenticationPrincipal User user, @PathVariable("locationId") int locationId, @RequestBody Location location) {
        isAuthorized(locationId);

        if (!user.isAdmin()) {
            if (!location.getBuilding().getInstitution().equals(user.getInstitution())) {
                throw new NotAuthorizedException("You are not authorized to update a location to an institution other than the one you belong to.");
            }
        }

        // Get the location that is currently in db
        Location cl = locationDao.getLocationById(locationId);

        // Make sure that only an admin could change the number of seats
        if (cl.getNumberOfSeats() != location.getNumberOfSeats() && !isAdmin())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Changing seats can only be done by admins");

        locationDao.updateLocation(location);
        logger.info(String.format("Location %d updated", locationId));
    }

    @PutMapping("/{locationId}/approval")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void approveLocation(@PathVariable("locationId") int locationId, @RequestBody LocationWithApproval landa) {
        locationDao.approveLocation(landa.getLocation(), landa.isApproval());
        logger.info(String.format("Location %d approved", locationId));
    }

    //authority user
    //the updated location should be part of an authority the user is part of.
    @DeleteMapping("/{locationId}")
    @PreAuthorize("@authorizedInstitutionController.hasAuthorityLocation(authentication.principal, #locationId)")
    public void deleteLocation(@PathVariable("locationId") int locationId) {
        isAuthorized(locationId);

        // Send email to all users who has a reservation for this location.
        List<LocationReservation> locationReservations = locationReservationDao.getAllFutureLocationReservationsOfLocation(locationId);
        for (LocationReservation locationReservation : locationReservations) {
            try {
                mailService.sendReservationSlotDeletedMessage(locationReservation.getUser().getMail(), locationReservation.getTimeslot());
            } catch (MessagingException e) {
                logger.error(String.format("Could not send mail to student %s about deleted reservation slot %s", locationReservation.getUser().getUsername(), locationReservation.getTimeslot().toString()));
            }
        }

        locationDao.deleteLocation(locationId);
        logger.info(String.format("Location %d deleted", locationId));
    }

    @PostMapping("/{locationId}/volunteers/{userId}")
    @PreAuthorize("@authorizedInstitutionController.hasAuthorityLocation(authentication.principal, #locationId)")
    public void addVolunteer(@PathVariable int locationId, @PathVariable String userId) {
        isAuthorized(locationId);
        volunteerDao.addVolunteer(locationId, userId);
    }

    @DeleteMapping("/{locationId}/volunteers/{userId}")
    @PreAuthorize("@authorizedInstitutionController.hasAuthorityLocation(authentication.principal, #locationId)")
    public void deleteVolunteer(@PathVariable int locationId, @PathVariable String userId) {
        isAuthorized(locationId);
        volunteerDao.deleteVolunteer(locationId, userId);
    }

    @GetMapping("/{locationId}/volunteers")
    @PreAuthorize("@authorizedInstitutionController.hasAuthorityLocation(authentication.principal, #locationId)")
    public List<User> getVolunteers(@PathVariable int locationId) {
        isAuthorized(locationId);
        return volunteerDao.getVolunteers(locationId);
    }

    // *****************************************
    // *   CRUD operations for LOCATION_TAGS   *
    // *****************************************

    /**
     * Following endpoint is a one-fits-all method: all tags that are supposed
     * to be set, must be provided in the body. Upon success only the tags
     * that have been provided, will be set for the location
     */
    @PutMapping("/tags/{locationId}")
    @PreAuthorize("@authorizedInstitutionController.hasAuthorityLocation(authentication.principal, #locationId)")
    public void setupTagsForLocation(@PathVariable("locationId") int locationId,
                                     @RequestBody List<Integer> tagIds) {
        isAuthorized(locationId);
        logger.info(String.format("Setting up the tags for location '%s' with ids [%s]",
                locationId, tagIds.stream().map(String::valueOf).collect(Collectors.joining(", "))));
        locationTagDao.deleteAllTagsFromLocation(locationId);
        locationTagDao.bulkAddTagsToLocation(locationId, tagIds);
    }

    // **************************************************
    // *   Equality queries concerning locations   *
    // **************************************************

    /**
     * Returns an array of 7 strings for each location that is opened in the week specified by the given
     * week number in the given year.
     * <p>
     * Each string is in the form of 'HH24:MI - HH24:MI' to indicate the opening and closing hour at
     * monday, tuesday, ..., sunday but can also be null to indicate that the location is not open that day.
     */
    @GetMapping("/overview/opening/{year}/{weekNr}")
    @PreAuthorize("permitAll()")
    public Map<String, String[]> getOpeningOverviewOfWeek(@PathVariable("year") int year,
                                                          @PathVariable("weekNr") int weekNr) {
        return locationDao.getOpeningOverviewOfWeek(year, weekNr);
    }

}
