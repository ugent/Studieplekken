package blok2.http.controllers;

import blok2.database.dao.ILocationDao;
import blok2.database.dao.ILocationReservationDao;
import blok2.database.dao.ITimeslotDao;
import blok2.exception.NoSuchDatabaseObjectException;
import blok2.extension.helpers.Base64String;
import blok2.extension.mail.MailService;
import blok2.http.controllers.authorization.AuthorizedLocationController;
import blok2.model.calendar.Timeslot;
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
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;

import static blok2.scheduling.PoolProcessor.RANDOM_RESERVATION_DURATION_MINS;

/**
 * This controller handles all requests related to location reservations.
 * Such as creating reservations, list of reservations, cancelling reservations,
 * scanning of users, ...
 */
@RestController
@RequestMapping("locations/reservations")
public class LocationReservationController extends AuthorizedLocationController {

    private final Logger logger = LoggerFactory.getLogger(LocationReservationController.class.getSimpleName());

    private final ILocationReservationDao locationReservationDao;
    private final ITimeslotDao timeslotDao;

    private final MailService mailService;
    private final ILocationDao locationDao;

    @Autowired
    public LocationReservationController(ILocationReservationDao locationReservationDao, ITimeslotDao timeslotDao, MailService ms
                                                , ILocationDao locDao) {
        this.locationReservationDao = locationReservationDao;
        this.timeslotDao = timeslotDao;
        this.mailService = ms;
        this.locationDao = locDao;
    }

    @GetMapping("/user")
    @PreAuthorize("(hasAuthority('USER') and #id == authentication.principal.userId) or hasAuthority('ADMIN')")
    // TODO: if only 'HAS_AUTHORITIES', then only allowed to retrieve the reservations for a location within one of the user's authorities
    // Not sure why you'd be allowed to get a user's reservations if you own a location.
    // TODO: We suddenly use a request parameter here. Probably better to streamline it with everything else and put it in the url.
    public List<LocationReservation> getLocationReservationsByUserId(@RequestParam String id) {
        return locationReservationDao.getAllLocationReservationsOfUser(id);
    }

    /**
     * Add a new reservation queue, to be processed later.
     * @return : The time at which the reservation will enter the 'fast' or 'non-random order' queue.
     *           This time may be in the past.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public LocalDateTime createLocationReservation(@AuthenticationPrincipal User user, @Valid @RequestBody Timeslot timeslot) {
        // TODO(ydndonck): Why do we need to get from db here? To prevent people from sending along malicious reservablefrom?
        // If that is the case then this check may no longer be needed as the actual checking and processing of reservations
        // now happens in a different thread and a 'naive' check based on the timeslot the user provided is sufficient (in this thread).
        // This could save a trip to the database, which makes this slightly faster.
        Timeslot dbTimeslot = timeslotDao.getTimeslot(timeslot.getTimeslotSeqnr());
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(dbTimeslot.getReservableFrom())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This timeslot can't yet be reserved");
        }
        LocationReservation reservation = new LocationReservation(user, dbTimeslot, LocationReservation.State.PENDING);
        if (!locationReservationDao.addLocationReservationToReservationQueue(reservation)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The timeslot was invalid.");
        }
        return dbTimeslot.getReservableFrom().plusMinutes(RANDOM_RESERVATION_DURATION_MINS);
    }

    @GetMapping("/timeslot/{seqnr}")
    @PreAuthorize("hasAuthority('HAS_VOLUNTEERS') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<LocationReservation> getLocationReservationsByTimeslot(
            @PathVariable("seqnr") int seqnr
    ) {
        Timeslot timeslot = timeslotDao.getTimeslot(seqnr);
        return locationReservationDao.getAllLocationReservationsOfTimeslot(timeslot);
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void deleteLocationReservation(@AuthenticationPrincipal User user, @RequestBody @Valid LocationReservation locationReservation) {
        LocationReservation dbLocationReservation = locationReservationDao.getLocationReservation(locationReservation.getUser().getUserId(), locationReservation.getTimeslot());

        checkAuthorization(
                (lr, u) -> hasAuthority(dbLocationReservation.getTimeslot().getLocationId()) || lr.getUser().getUserId().equals(u.getUserId()),
                dbLocationReservation
        );
        Timeslot timeslot = dbLocationReservation.getTimeslot();
        LocalDateTime closingHour = timeslot.timeslotDate().atTime(timeslot.getClosingHour());
        if (closingHour.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The timeslot has already been closed.");
        }
        // Throw exception back if user is already scanned as present.
        if (!dbLocationReservation.getUser().getUserId().equals(user.getUserId())) {
            if (dbLocationReservation.getStateE() == LocationReservation.State.PRESENT) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user is already scanned as present for this timeslot.");
            }
        }

        locationReservationDao.deleteLocationReservation(dbLocationReservation);
        logger.info(String.format("LocationReservation for user %s at time %s deleted", dbLocationReservation.getUser(), dbLocationReservation.getTimeslot().toString()));

        // Send email to student if student is not the user who requested the deletion.
        if (!dbLocationReservation.getUser().getUserId().equals(user.getUserId())) {
            try {
                mailService.sendReservationSlotDeletedMessage(dbLocationReservation.getUser().getMail(), dbLocationReservation.getTimeslot());
            } catch (MessagingException | UnsupportedEncodingException e) {
                logger.error(String.format("Could not send mail to student %s about deleted reservation slot %s", user.getUsername(), dbLocationReservation.getTimeslot().toString()));
            }
        }
    }

    @PostMapping("/{userid}/{seqnr}/attendance")
    @PreAuthorize("hasAuthority('HAS_VOLUNTEERS') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void setLocationReservationAttendance(
            @PathVariable("seqnr") int seqnr,
            @PathVariable("userid") String encodedId,
            @RequestBody LocationReservation.AttendedPostBody body
    ) {
        String userid = Base64String.base64Decode(encodedId);
        Timeslot slot = timeslotDao.getTimeslot( seqnr);
        isVolunteer(locationDao.getLocationById(slot.getLocationId()));
        if (!locationReservationDao.setReservationAttendance(userid, slot, body.getAttended()))
            throw new NoSuchDatabaseObjectException("No such reservation");
    }

    @PutMapping("/not-scanned")
    @PreAuthorize("hasAuthority('HAS_VOLUNTEERS') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void setAllNotScannedStudentsToUnattendedForTimeslot(@RequestBody Timeslot timeslot) {
        // check if user is allowed by role
        isVolunteer(locationDao.getLocationById(timeslot.getLocationId()));

        logger.info(String.format("Setting all students who were not scanned to unattended for timeslot %s", timeslot));
        locationReservationDao.setNotScannedStudentsToUnattended(timeslot);
    }

}
