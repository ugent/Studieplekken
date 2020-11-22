package blok2.helpers.authorization;

import blok2.daos.IAuthorityDao;
import blok2.model.Authority;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.SQLException;
import java.util.List;

public abstract class AuthorizedLocationController extends AuthorizedController {

    @Autowired
    IAuthorityDao authorityDao;

    public void isAuthorized(String locationName) {
        isAuthorized((l, $) -> hasAuthority(l), locationName, "You do not hold the correct Authority.");
    }

    protected boolean hasAuthority(String locationName) {
        try {
            User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Location> allLocations = authorityDao.getLocationsInAuthoritiesOfUser(user.getAugentID());
            return allLocations.stream().anyMatch(l -> l.getName().equals(locationName));
        } catch (SQLException throwables) {
            return false;
        }
    }

    protected boolean hasAuthority(Authority authority) {
        try {
            User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Authority> allAuthorities = authorityDao.getAuthoritiesFromUser(user.getAugentID());
            return allAuthorities.contains(authority);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }
}