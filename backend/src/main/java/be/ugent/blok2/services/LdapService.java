package be.ugent.blok2.services;

import be.ugent.blok2.helpers.exceptions.LdapException;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class LdapService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LdapTemplate ldapTemplate;

    @Autowired
    public LdapService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    /**
     * Query the AUGent LDAP to retrieve the user associated with the given mail.
     * Note that the userObject.setPassword() setter will not be called, this is important to
     * note for the AccountController. The controller needs to encode the password itself
     */
    public List<User> searchUserByMail(String mail) throws LdapException {
        System.out.println(ldapTemplate);

        try {
            return ldapTemplate.search("ou=people", "mail=" + mail, (AttributesMapper<User>) attrs -> {
                logger.info("Queried AUGent LDAP for: " + attrs.toString());
                User u = new User();
                u.setFirstName(attrs.get("givenName").get().toString());
                u.setLastName(attrs.get("sn").get().toString());
                u.setMail(attrs.get("mail").get().toString().toLowerCase());
                // Note: u.setPassword(...) will not be set
                u.setInstitution(attrs.get("o").get().toString());
                u.setAugentID(attrs.get("augentID").get().toString());
                u.setRoles(new Role[]{Role.STUDENT});
                return u;
            });
        } catch (Exception e) {
            System.out.println();
            throw new LdapException("ERROR: retrieving user from LDAP with mail "
                    + mail + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }
}
