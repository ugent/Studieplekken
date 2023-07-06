package blok2.http.authorization;

import blok2.database.daos.IAuthorityDao;
import blok2.model.Authority;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public abstract class AuthorizedLocationController extends AuthorizedController {

    @Autowired
    IAuthorityDao authorityDao;

    public void isAuthorized(int locationId) {
        isAuthorized((l, $) -> hasAuthority(l), locationId, "You do not hold the correct Authority.");
    }

    public void isVolunteer(Location location) {
        isAuthorized(
            (l, $) -> hasAuthority(location.getLocationId()) || isVolunteerForLoc(location),
            location.getLocationId(),
            "You do not hold the correct Authority."
        );
    }

    protected boolean hasAuthority(int locationId) {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Location> allLocations = authorityDao.getLocationsInAuthoritiesOfUser(user.getUserId());
        return allLocations.stream().anyMatch(l -> l.getLocationId() == locationId);
    }

    protected boolean hasAuthority(Authority authority) {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Authority> allAuthorities = authorityDao.getAuthoritiesFromUser(user.getUserId());
        return allAuthorities.contains(authority);
    }

    protected boolean isVolunteerForLoc(Location location) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getUserVolunteer().stream().anyMatch(l -> l.equals(location));
    }
}
