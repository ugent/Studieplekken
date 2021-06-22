package blok2.controllers;

import blok2.daos.orm.LocationRepository;
import blok2.helpers.authorization.AuthorizedLocationController;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("scan")
public class ScanController extends AuthorizedLocationController {

    private final LocationRepository locationRepository;

    @Autowired
    public ScanController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @GetMapping("/locations")
    @PreAuthorize("hasAuthority('HAS_VOLUNTEERS') or hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public List<Location> getLocationsToScan() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return locationRepository.findAllActiveLocations().stream().filter(l ->
                user.isAdmin() ||
                user.getUserAuthorities().contains(l.getAuthority()) ||
                user.getUserVolunteer().contains(l)
        ).collect(Collectors.toList());
    }

}
