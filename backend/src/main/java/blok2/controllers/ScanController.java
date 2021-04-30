package blok2.controllers;

import blok2.daos.ILocationDao;
import blok2.helpers.authorization.AuthorizedLocationController;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("scan")
public class ScanController extends AuthorizedLocationController {

    private final Logger logger = LoggerFactory.getLogger(ScanController.class);

    private final ILocationDao locationDao;

    @Autowired
    public ScanController(ILocationDao locationDao) {
        this.locationDao = locationDao;
    }

    @GetMapping("/locations")
    @PreAuthorize("hasAuthority('HAS_VOLUNTEERS') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<Location> getLocationsToScan() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            return locationDao.getAllLocations().stream().filter(f ->
                    user.isAdmin() ||
                    user.getUserAuthorities().contains(f.getAuthority()) ||
                    user.getUserVolunteer().contains(f.getLocationId())
            ).collect(Collectors.toList());
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

}
