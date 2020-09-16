package blok2.security;

import blok2.daos.IAccountDao;
import blok2.model.users.User;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class CustomUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

    private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final IAccountDao accountDao;

    public CustomUserDetailsService(IAccountDao accountDao) {
        this.accountDao = accountDao;
    }

    /**
     * TODO
     * Flow of loading a user:
     *   1. try to retrieve from the application database
     *        - if the retrieved user is not null, return the retrieved user
     *        - if the retrieved user is null, go to 2
     *   2. create a new user based on the UGent LDAP
     */
    @Override
    public UserDetails loadUserDetails(CasAssertionAuthenticationToken casAssertionAuthenticationToken) throws UsernameNotFoundException {
        AttributePrincipal principal = casAssertionAuthenticationToken.getAssertion().getPrincipal();
        String mail = (String) principal.getAttributes().get("mail");

        User user;

        try {
            user = this.accountDao.getUserByEmail(mail);
        } catch (SQLException e) {
            logger.error("SQL exception while getting a user to login", e);
            throw new UsernameNotFoundException("Database error");
        }

        if (user != null) {
            return user;
        }

        // TODO: create new user using the UGent LDAP

        throw new UsernameNotFoundException("User with mail '" + mail + "' not found");
    }

}
