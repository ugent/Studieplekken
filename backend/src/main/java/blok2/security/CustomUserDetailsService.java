package blok2.security;

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

        // Try to find the user with given mail in the application database

        try {
            user = this.accountDao.getUserByEmail(mail);
        } catch (SQLException e) {
            logger.error("SQL exception while getting a user to login", e);
            throw new UsernameNotFoundException("Database error");
        }

        if (user != null) {
            return user;
        }

        // Create new user using the UGent LDAP

        user = getUserFromLdapByMail(mail);

        if (user != null) {
            try {
                accountDao.directlyAddUser(user);
                return user;
            } catch (SQLException e) {
                logger.error("SQL exception while adding the new user from LDAP info to the application database", e);
            }
        }

        logger.error("Unable to find/add a user with mail '" + mail + "'");
        throw new UsernameNotFoundException("Unable to find/add a user with mail '" + mail + "'");
    }

    public User getUserFromLdapByMail(String mail) {
        try {
            List<User> users = ldapTemplate.search("ou=people", "mail=" + mail, (AttributesMapper<User>) attrs -> {
                User user = new User();
                user.setFirstName(attrs.get("givenName").get().toString());
                user.setLastName(attrs.get("sn").get().toString());
                user.setMail(attrs.get("mail").get().toString());
                user.setPassword("secret");
                user.setInstitution("UGent");

                if (attrs.get("ugentID") != null) {
                    user.setAugentID(attrs.get("ugentID").get().toString());
                } else if (attrs.get("ugentStudentID") != null) {
                    user.setAugentID(attrs.get("ugentStudentID").get().toString());
                } else {
                    // worst case: set mail instead of ugent/student id
                    user.setAugentID(attrs.get("mail").get().toString());
                }

                return user;
            });

            if (users.size() != 1) {
                return null;
            }

            return users.get(0);
        } catch (Exception e) {
            logger.error("Exception thrown upon querying LDAP for new user with mail: '" + mail + "'", e);
            return null;
        }
    }

}
