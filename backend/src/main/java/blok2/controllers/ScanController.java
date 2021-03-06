package blok2.controllers;

import blok2.daos.IAccountDao;
import blok2.daos.ILocationDao;
import blok2.model.reservables.Location;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("scan")
public class ScanController {

    private final Logger logger = LoggerFactory.getLogger(ScanController.class);

    private final ILocationDao locationDao;
    private final IAccountDao accountDao;

    @Autowired
    public ScanController(ILocationDao locationDao, IAccountDao accountDao) {
        this.locationDao = locationDao;
        this.accountDao = accountDao;
    }

    // TODO: fully implement this endpoint
    @GetMapping("/locations")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Location> getLocationsToScan() {
        try {
            return locationDao.getAllLocations();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    // TODO: fully implement this endpoint
    @GetMapping("/users/{locationId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<User> getUsersToScanAtLocation(@PathVariable("locationId") int locationId) {
        try {
            return Stream.concat(accountDao.getUsersByLastName("Van de Walle").stream(), accountDao.getUsersByLastName("Geldhof").stream()).collect(Collectors.toList());
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

}
