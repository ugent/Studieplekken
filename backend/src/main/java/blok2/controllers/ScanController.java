package blok2.controllers;

import blok2.daos.ILocationDao;
import blok2.model.reservables.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("scan")
public class ScanController {

    private final Logger logger = LoggerFactory.getLogger(ScanController.class);

    private final ILocationDao locationDao;

    @Autowired
    public ScanController(ILocationDao locationDao) {
        this.locationDao = locationDao;
    }

    @GetMapping("/locations")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    // TODO: specify the has_authorities better in code
    public List<Location> getLocationsToScan() {
        try {
            return locationDao.getAllLocations();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

}
