package be.ugent.blok2.security.UserDetailsServices;

import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.helpers.exceptions.LdapException;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.services.LdapService;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.sql.SQLException;
import java.util.List;

/**
 * Be very careful creating an instance of this class. The IAccountDao and LdapService should be
 * injected by Spring. E.g. like in Blok2Application.casAuthenticationProvider() Bean method
 */
public class CasUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final IAccountDao accountDao;
    private final LdapService ldapService;

    public CasUserDetailsService(IAccountDao accountDao, LdapService ldapService) {
        this.accountDao = accountDao;
        this.ldapService = ldapService;
    }

    @Override
    public UserDetails loadUserDetails(CasAssertionAuthenticationToken casAssertionAuthenticationToken) throws UsernameNotFoundException {
        AttributePrincipal principal = casAssertionAuthenticationToken.getAssertion().getPrincipal();
        String mail = (String) principal.getAttributes().get("mail");

        // Check if the UGent user has logged in before and therefore has a specific account within the application
        User user;

        try {
            user = accountDao.getUserByEmail(mail);
        } catch (SQLException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }

        if (user != null)
            return user;

        // If not, the UGent user should be added to the application's specific database
        try {
            List<User> users = ldapService.searchUserByMail(mail);

            if (users.size() != 1)
                throw new UsernameNotFoundException("No (most likely) or multiple (not likely) users found in AUGent LDAP with email: " + mail);

            user = users.get(0);

        } catch (LdapException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }

        try {
            user = accountDao.directlyAddUser(user);
        } catch (SQLException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
        logger.info("First time UGhent user logged in -> adding user to database: " + user);

        return user;
    }
}
