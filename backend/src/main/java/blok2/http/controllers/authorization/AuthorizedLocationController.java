package blok2.http.controllers.authorization;

import blok2.database.dao.IAuthorityDao;
import blok2.model.Authority;
import blok2.model.location.Location;
import blok2.model.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public abstract class AuthorizedLocationController extends AuthorizedController {
    @Autowired
    IAuthorityDao authorityDao;

    /**
     * Check if the user has the correct authority for the location.
     * 
     * @param locationId The location id to check the authority for.
     */
    public void checkLocationAuthorization(int locationId) {
        checkAuthorization((l, $) -> hasAuthority(l), locationId, "You do not hold the correct Authority.");
    }

    /**
     * Check if the user is a volunteer for the location.
     * 
     * @param location The location to check if the user is a volunteer for.
     */
    public void isVolunteer(Location location) {
        checkAuthorization(
            (l, $) -> hasAuthority(location.getLocationId()) || isVolunteerForLoc(location),
            location.getLocationId(),
            "You do not hold the correct Authority."
        );
    }

    /**
     * Check if the user has an authority for the location.
     * 
     * @param locationId The location id to check the authority for.
     * @return True if the user has an authority for the location.
     */
    protected boolean hasAuthority(int locationId) {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Location> allLocations = authorityDao.getLocationsInAuthoritiesOfUser(user.getUserId());
        return allLocations.stream().anyMatch(l -> l.getLocationId() == locationId);
    }

    /**
     * Check if the user has an authority.
     * 
     * @param authority The authority to check for.
     * @return True if the user has the authority.
     */
    protected boolean hasAuthority(Authority authority) {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Authority> allAuthorities = authorityDao.getAuthoritiesFromUser(user.getUserId());
        return allAuthorities.contains(authority);
    }

    /**
     * Check if the user is a volunteer for the location.
     * 
     * @param location The location to check if the user is a volunteer for.
     * @return True if the user is a volunteer for the location.
     */
    protected boolean isVolunteerForLoc(Location location) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getUserVolunteer().stream().anyMatch(l -> l.equals(location));
    }
}
