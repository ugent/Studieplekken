package blok2.controllers;

import blok2.daos.ILockerReservationDao;
import blok2.helpers.authorization.AuthorizedLocationController;
import blok2.model.reservations.LockerReservation;
import blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * This controller handles all requests related to locker reservations.
 * Such as creating reservations, list of reservations, cancelling reservations, ...
 */
@RestController
@RequestMapping("lockers/reservations")
public class LockerReservationController extends AuthorizedLocationController {

    private final Logger logger = LoggerFactory.getLogger(LockerReservation.class.getSimpleName());

    private final ILockerReservationDao lockerReservationDao;

    @Autowired
    public LockerReservationController(ILockerReservationDao lockerReservationDao) {
        this.lockerReservationDao = lockerReservationDao;
    }

    @GetMapping("/user")
    @PreAuthorize("(hasAuthority('USER') and #id == authentication.principal.augentID) or " +
            "hasAuthority('ADMIN')")
    // TODO: if only 'HAS_AUTHORITIES', then only allowed to retrieve the reservations for a location within one of the user's authorities
    public List<LockerReservation> getLockerReservationsOfUserById(@RequestParam String id) {
        try {
            return lockerReservationDao.getAllLockerReservationsOfUser(id);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/location")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<LockerReservation> getLocationReservationsOfLocation(@RequestParam String locationName,
                                                                     @RequestParam boolean pastReservations) {
        isAuthorized(locationName);
        try {
            return lockerReservationDao.getAllLockerReservationsOfLocation(locationName, pastReservations);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void updateLockerReservation(@RequestBody LockerReservation lockerReservation) {
        isAuthorized(
                (lr, user) -> hasAuthority(lr.getLocker().getLocation().getName()) || lr.getOwner().getAugentID().equals(user.getAugentID()),
                lockerReservation
        );
        try {
            lockerReservationDao.changeLockerReservation(lockerReservation);
            logger.info(String.format("Updating locker reservation by owner %s for locker %d in location %s", lockerReservation.getOwner(), lockerReservation.getLocker().getNumber(), lockerReservation.getLocker().getLocation().getName()));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void deleteLockerReservation(@RequestBody LockerReservation lockerReservation) {
        isAuthorized(
                (lr, user) -> hasAuthority(lr.getLocker().getLocation().getName()) || lr.getOwner().getAugentID().equals(user.getAugentID()),
                lockerReservation
        );
        try {
            lockerReservationDao.deleteLockerReservation(lockerReservation.getLocker().getLocation().getName(),
                    lockerReservation.getLocker().getNumber());
            logger.info(String.format("Deleting locker reservation by owner %s for locker %d in location %s", lockerReservation.getOwner(), lockerReservation.getLocker().getNumber(), lockerReservation.getLocker().getLocation().getName()));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}
