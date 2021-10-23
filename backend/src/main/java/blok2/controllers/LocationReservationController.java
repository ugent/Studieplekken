package blok2.controllers;

import blok2.daos.ILocationDao;
import blok2.daos.ILocationReservationDao;
import blok2.daos.ITimeslotDao;
import blok2.helpers.authorization.AuthorizedLocationController;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
import blok2.helpers.exceptions.NotAuthorizedException;
import blok2.mail.MailService;
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
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @PostMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public LocationReservation createLocationReservation(@AuthenticationPrincipal User user, @Valid @RequestBody Timeslot timeslot) {
        try {
            Timeslot dbTimeslot = timeslotDao.getTimeslot(timeslot.getTimeslotSeqnr());
            LocationReservation reservation = new LocationReservation(user, dbTimeslot, LocationReservation.State.APPROVED);
            if (LocalDateTime.now().isBefore(dbTimeslot.getReservableFrom())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This timeslot can't yet be reserved");
            }
            if (!locationReservationDao.addLocationReservationIfStillRoomAtomically(reservation)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "There are no more spots left for this location.");
            }
            return locationReservationDao.getLocationReservation(user.getUserId(), timeslot);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
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

        isAuthorized(
                (lr, u) -> hasAuthority(dbLocationReservation.getTimeslot().getLocationId()) || lr.getUser().getUserId().equals(u.getUserId()),
                dbLocationReservation
        );

        locationReservationDao.deleteLocationReservation(dbLocationReservation);
        logger.info(String.format("LocationReservation for user %s at time %s deleted", dbLocationReservation.getUser(), dbLocationReservation.getTimeslot().toString()));

        // Send email to student if student is not the user who requested the deletion.
        if (!dbLocationReservation.getUser().getUserId().equals(user.getUserId())) {
            try {
                mailService.sendReservationSlotDeletedMessage(dbLocationReservation.getUser().getMail(), dbLocationReservation.getTimeslot());
            } catch (MessagingException e) {
                logger.error(String.format("Could not send mail to student %s about deleted reservation slot %s", user.getUsername(), dbLocationReservation.getTimeslot().toString()));
            }
        }
    }

    @PostMapping("/{userid}/{seqnr}/attendance")
    @PreAuthorize("hasAuthority('HAS_VOLUNTEERS') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void setLocationReservationAttendance(
            @PathVariable("seqnr") int seqnr,
            @PathVariable("userid") String userid,
            @RequestBody LocationReservation.AttendedPostBody body
    ) {
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
