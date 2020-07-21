package be.ugent.blok2.security.UserDetailsServices;

import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.helpers.Institution;
import be.ugent.blok2.helpers.Resources;
import be.ugent.blok2.helpers.exceptions.NoSuchUserException;
import be.ugent.blok2.helpers.exceptions.NoUserLoggedInWithGivenSessionIdMappingException;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.security.UsersCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Service
public class BlokAtUGentUserDetailsService implements UserDetailsService {

    private final IAccountDao accountDao;

    public BlokAtUGentUserDetailsService(IAccountDao accountDao) {
        this.accountDao = accountDao;
    }

    /**
     * This method searches for the user with the given email address.
     * It is called by the BlokAtUGentAuthenicationProvider's authenticate() method
     * when a user tries to login.
     * Note that the authenticate() method is provided two classes above
     * BlokAtUGentAuthenticationProvider (which is just a convenience class)
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = null;
        try {
            user = accountDao.getUserByEmail(email);
        } catch (SQLException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
        if (user == null) {
            throw new UsernameNotFoundException("User " + email + " not found!");
        }

        // reden voor tweede predicaat: UGent gebruikers moeten inloggen met CAS
        // anders is er een serieus veiligheidsprobleem, zie commentaar bij de
        // methode directlyAddUser(User) van de klasse DBAccountDao
        if (Institution.UGent.equals(user.getInstitution())
                && !allowedUGhentUsersToLoginWithoutCas().contains(user.getMail())) {
            throw new UsernameNotFoundException("User " + email + " his/her institution is UGent -> use CAS!");
        }
        return user;
    }

    private Set<String> allowedUGhentUsersToLoginWithoutCas() {
        String csv = Resources.applicationProperties.getString("allowed-ugent-users-to-login-without-cas");
        return new HashSet<>(Arrays.asList(csv.split(";")));
    }

}
