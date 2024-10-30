package blok2.http.controllers;

import blok2.database.dao.*;
import blok2.exception.AlreadyExistsException;
import blok2.extension.helpers.LocationWithApproval;
import blok2.extension.helpers.View;
import blok2.extension.mail.MailService;
import blok2.extension.orm.LocationNameAndNextReservableFrom;
import blok2.http.controllers.authorization.AuthorizedLocationController;
import blok2.model.ActionLogEntry;
import blok2.model.ActionLogEntry.Domain;
import blok2.model.ActionLogEntry.Type;
import blok2.model.location.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import com.fasterxml.jackson.annotation.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
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

    private final Logger logger = LoggerFactory.getLogger(
        LocationController.class.getSimpleName()
    );

    private final ILocationDao locationDao;
    private final ILocationTagDao locationTagDao;
    private final IVolunteerDao volunteerDao;
    private final ILocationReservationDao locationReservationDao;
    private final IActionLogDao actionLogDao;
    private final IUserLocationSubscriptionDao userLocationSubscriptionDao;

    private final MailService mailService;

    @Autowired
    public LocationController(
        ILocationDao locationDao, 
        ILocationTagDao locationTagDao,
        IVolunteerDao volunteerDao, 
        MailService mailService, 
        ILocationReservationDao locationReservationDao,
        IActionLogDao actionLogDao, 
        IUserLocationSubscriptionDao userLocationSubscriptionDao
    ) {
        this.locationDao = locationDao;
        this.locationTagDao = locationTagDao;
        this.mailService = mailService;
        this.volunteerDao = volunteerDao;
        this.locationReservationDao = locationReservationDao;
        this.actionLogDao = actionLogDao;
        this.userLocationSubscriptionDao = userLocationSubscriptionDao;
    }

    @JsonView(View.List.class)
    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Location> getAllActiveLocations() {
        List<Location> locations = locationDao.getAllActiveLocations();
        locations.sort(Comparator.comparing(Location::getName));
        return locations;
    }

    @JsonView(View.List.class)
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Location> getAllLocations() {
        List<Location> locations = locationDao.getAllLocations();
        locations.sort(Comparator.comparing(Location::getName));
        return locations;
    }

    @JsonView(View.List.class)
    @GetMapping("/unapproved")
    public List<Location> getAllUnapprovedLocations() {
        return locationDao.getAllUnapprovedLocations();
    }

    @JsonView(View.Detail.class)
    @GetMapping("/{locationId}")
    @PreAuthorize("permitAll()")
    public Location getLocation(@PathVariable("locationId") int locationId, @AuthenticationPrincipal User user) {
        Location location = locationDao.getLocationById(locationId);
        locationDao.initializeTags(location);
        userLocationSubscriptionDao.initializeSubscribed(location, user);
        return location;
    }

    @GetMapping("/nextReservableFroms")
    @PreAuthorize("permitAll()")
    public List<LocationNameAndNextReservableFrom> getAllNextReservableFroms() {
        return locationDao.getNextReservationMomentsOfAllLocations();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void addLocation(@AuthenticationPrincipal User user, @RequestBody Location location) {
        // Only authorize if the user is an admin or has the authority to add locations.
        this.checkAuthorization((loc, $) -> hasAuthority(loc.getAuthority()), location);

        // Make sure the location does not already exist.
        if (locationDao.existsLocationByName(location.getName())) {
            throw new AlreadyExistsException("Location name already in use");
        }

        // Add the location to the database.
        this.locationDao.addLocation(location);

        // Log the action.
        this.actionLogDao.addLogEntry(
            new ActionLogEntry(Type.INSERTION, user, Domain.LOCATION, location.getLocationId())
        );
    }

    @PutMapping("/{locationId}")
    @PreAuthorize("@authorizedInstitutionController.hasAuthorityLocation(authentication.principal, #locationId)")
    public void updateLocation(@AuthenticationPrincipal User user, @PathVariable("locationId") int locationId, @RequestBody Location location) {
        this.checkLocationAuthorization(locationId);

        this.locationDao.updateLocation(location);

        this.actionLogDao.addLogEntry(
            new ActionLogEntry(Type.UPDATE, user, Domain.LOCATION, locationId)
        );
    }

    @PutMapping("/{locationId}/approval")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void approveLocation(@PathVariable("locationId") int locationId, @RequestBody LocationWithApproval landa, @AuthenticationPrincipal User user) {
        ActionLogEntry logEntry = new ActionLogEntry(ActionLogEntry.Type.OTHER, user, ActionLogEntry.Domain.LOCATION, locationId);
        actionLogDao.addLogEntry(logEntry);
        locationDao.approveLocation(landa.getLocation(), landa.isApproval());
    }

    //authority user
    //the updated location should be part of an authority the user is part of.
    @DeleteMapping("/{locationId}")
    @PreAuthorize("@authorizedInstitutionController.hasAuthorityLocation(authentication.principal, #locationId)")
    public void deleteLocation(@PathVariable("locationId") int locationId, @AuthenticationPrincipal User user) {
        ActionLogEntry logEntry = new ActionLogEntry(ActionLogEntry.Type.DELETION, user, ActionLogEntry.Domain.LOCATION, locationId);
        actionLogDao.addLogEntry(logEntry);
        checkLocationAuthorization(locationId);

        // Send email to all users who has a reservation for this location.
        List<LocationReservation> locationReservations = locationReservationDao.getAllFutureLocationReservationsOfLocation(locationId);
        for (LocationReservation locationReservation : locationReservations) {
            try {
                mailService.sendReservationSlotDeletedMessage(locationReservation.getUser().getMail(), locationReservation.getTimeslot());
            } catch (MessagingException | UnsupportedEncodingException e) {
                logger.error(String.format("Could not send mail to student %s about deleted reservation slot %s", locationReservation.getUser().getUsername(), locationReservation.getTimeslot().toString()));
            }
        }

        locationDao.deleteLocation(locationId);
        logger.info(String.format("Location %d deleted", locationId));
    }

    @PostMapping("/{locationId}/volunteers/{userId}")
    @PreAuthorize("@authorizedInstitutionController.hasAuthorityLocation(authentication.principal, #locationId)")
    public void addVolunteer(@PathVariable int locationId, @PathVariable String userId, @AuthenticationPrincipal User user) {
        ActionLogEntry logEntry = new ActionLogEntry(ActionLogEntry.Type.OTHER, user, ActionLogEntry.Domain.LOCATION, locationId);
        actionLogDao.addLogEntry(logEntry);
        checkLocationAuthorization(locationId);
        volunteerDao.addVolunteer(locationId, userId);
    }

    @DeleteMapping("/{locationId}/volunteers/{userId}")
    @PreAuthorize("@authorizedInstitutionController.hasAuthorityLocation(authentication.principal, #locationId)")
    public void deleteVolunteer(@PathVariable int locationId, @PathVariable String userId, @AuthenticationPrincipal User user) {
        ActionLogEntry logEntry = new ActionLogEntry(ActionLogEntry.Type.OTHER, user, ActionLogEntry.Domain.LOCATION, locationId);
        actionLogDao.addLogEntry(logEntry);
        checkLocationAuthorization(locationId);
        volunteerDao.deleteVolunteer(locationId, userId);
    }

    @GetMapping("/{locationId}/volunteers")
    public List<User> getVolunteers(@PathVariable int locationId) {
        checkLocationAuthorization(locationId);
        return volunteerDao.getVolunteers(locationId);
    }

    /**
     * Following endpoint is a one-fits-all method: all tags that are supposed
     * to be set, must be provided in the body. Upon success only the tags
     * that have been provided, will be set for the location
     */
    @PutMapping("/tags/{locationId}")
    @PreAuthorize("@authorizedInstitutionController.hasAuthorityLocation(authentication.principal, #locationId)")
    public void setupTagsForLocation(@PathVariable("locationId") int locationId,
                                     @RequestBody List<Integer> tagIds) {
        checkLocationAuthorization(locationId);
        logger.info(String.format("Setting up the tags for location '%s' with ids [%s]",
                locationId, tagIds.stream().map(String::valueOf).collect(Collectors.joining(", "))));
        locationTagDao.deleteAllTagsFromLocation(locationId);
        locationTagDao.bulkAddTagsToLocation(locationId, tagIds);
    }

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

    @PostMapping("/{locationId}/subscriptions")
    public void subscribeToLocation(@PathVariable int locationId, @AuthenticationPrincipal User user) {
        Location location = locationDao.getLocationById(locationId);
        userLocationSubscriptionDao.subscribeToLocation(location, user);
    }

    @DeleteMapping("/{locationId}/subscriptions")
    public void unsubscribeFromLocation(@PathVariable int locationId, @AuthenticationPrincipal User user) {
        Location location = locationDao.getLocationById(locationId);
        userLocationSubscriptionDao.unsubscribeFromLocation(location, user);
    }
}
