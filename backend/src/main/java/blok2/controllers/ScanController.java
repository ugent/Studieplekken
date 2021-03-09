package blok2.controllers;

import blok2.daos.IAccountDao;
import blok2.daos.ILocationDao;
import blok2.daos.ILocationReservationDao;
import blok2.helpers.authorization.AuthorizedLocationController;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("scan")
public class ScanController extends AuthorizedLocationController {

    private final Logger logger = LoggerFactory.getLogger(ScanController.class);

    private final ILocationDao locationDao;
    private final IAccountDao accountDao;
    private final ILocationReservationDao reservationDao;

    @Autowired
    public ScanController(ILocationDao locationDao, IAccountDao accountDao, ILocationReservationDao reservationDao) {
        this.locationDao = locationDao;
        this.accountDao = accountDao;
        this.reservationDao = reservationDao;
    }

    @GetMapping("/locations")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Location> getLocationsToScan() {
        try {
            return locationDao.getAllLocations().stream()
                    .filter(f -> f.getCurrentTimeslot() != null)
                    .filter(f -> true)            // TODO: volunteer statute
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/users/{locationId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<User> getUsersToScanAtLocation(@PathVariable("locationId") int locationId) {
        try {
            Timeslot timeslot = locationDao.getLocationById(locationId).getCurrentTimeslot();
            if(timeslot == null)
                return Collections.emptyList();

            return reservationDao.getAllLocationReservationsOfTimeslot(timeslot).stream().map(LocationReservation::getUser).collect(Collectors.toList());
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

}
