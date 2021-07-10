package blok2.security.services;

import blok2.daos.IUserDao;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
import blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomSamlUserDetailsService implements SAMLUserDetailsService {

    private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final IUserDao userDao;

    @Autowired
    public CustomSamlUserDetailsService(IUserDao userDao) {
        this.userDao = userDao;
    }


    @Override
    public User loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {

        User user;
        try {
            user = this.userDao.getUserByEmail(credential.getNameID().getValue());
        } catch(NoSuchDatabaseObjectException e) {
            user = new User();
            user.setMail(credential.getNameID().getValue());
            user.setInstitution("UGent");
            user.setUserId(UUID.randomUUID().toString());
            userDao.addUser(user);
        }
        return user;
    }
}
