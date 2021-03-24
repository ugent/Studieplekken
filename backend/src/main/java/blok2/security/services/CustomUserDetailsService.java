package blok2.security.services;

import blok2.daos.IAccountDao;
import blok2.model.users.User;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Service
public class CustomUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

    private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final IAccountDao accountDao;
    private final LdapTemplate ldapTemplate;

    @Autowired
    public CustomUserDetailsService(IAccountDao accountDao,
                                    LdapTemplate ldapTemplate) {
        this.accountDao = accountDao;
        this.ldapTemplate = ldapTemplate;
    }

    /**
     * Flow of loading a user:
     *   1. try to retrieve from the application database
     *        - if the retrieved user is not null, return the retrieved user
     *        - if the retrieved user is null, go to 2
     *   2. create a new user based on the UGent LDAP
     */
    @Override
    public UserDetails loadUserDetails(CasAssertionAuthenticationToken casAssertionAuthenticationToken) throws UsernameNotFoundException {
        AttributePrincipal principal = casAssertionAuthenticationToken.getAssertion().getPrincipal();
        String ugentID = (String) principal.getAttributes().get("ugentID");
        String mail = (String) principal.getAttributes().get("mail");

        User user;

        // Try to find the user with given mail in the application database
        try {
            user = this.accountDao.getUserById(ugentID);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new UsernameNotFoundException("Database error");
        }

        if (user != null) {
            return user;
        }

        // Create new user using the UGent LDAP
        user = getUserFromLdap(ugentID, mail);

        if (user != null) {
            try {
                accountDao.directlyAddUser(user);
                return user;
            } catch (SQLException e) {
                logger.error(e.getMessage());
                logger.error(Arrays.toString(e.getStackTrace()));
            }
        }

        logger.error(String.format("Unable to find/add a user with ugentID '%s' and mail '%s'", ugentID, mail));
        throw new UsernameNotFoundException
                (String.format("Unable to find/add a user with ugentID '%s' and mail '%s'", ugentID, mail));
    }

    public User getUserFromLdap(String ugentID, String mail) {
        try {
            List<User> users = ldapTemplate.search("ou=people", "mail=" + mail, (AttributesMapper<User>) attrs -> {
                User user = new User();

                // try to use 'ugentPreferredGivenName' and use 'givenName' as fallback
                try {
                    user.setFirstName(attrs.get("ugentPreferredGivenName").get().toString());
                } catch (NullPointerException ignore) {
                    user.setFirstName(attrs.get("givenName").get().toString());
                }

                // try to use 'ugentPreferredSn' and use 'sn' as fallback
                try {
                    user.setLastName(attrs.get("ugentPreferredSn").get().toString());
                } catch (NullPointerException ignore) {
                    user.setLastName(attrs.get("sn").get().toString());
                }

                user.setMail(attrs.get("mail").get().toString());
                user.setPassword("secret");
                user.setInstitution("UGent");
                user.setAugentID(ugentID);
                return user;
            });

            if (users.size() != 1) {
                return null;
            }

            return users.get(0);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

}
