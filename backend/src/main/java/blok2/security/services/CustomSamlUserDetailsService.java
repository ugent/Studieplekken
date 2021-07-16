package blok2.security.services;

import blok2.daos.IUserDao;
import blok2.helpers.Institution;
import blok2.helpers.exceptions.InvalidRequestParametersException;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
import blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CustomSamlUserDetailsService implements SAMLUserDetailsService {

    private final Map<String, String> idpToInstitution;
    private final String hoGentIdp;
    private final String arteveldeHSIdp;

    private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final IUserDao userDao;

    @Autowired
    public CustomSamlUserDetailsService(IUserDao userDao,
                                        @Value("${saml.idps.okta}") String oktaIdp,
                                        @Value("${saml.idps.ssoCircle}") String ssoCircleIdp,
                                        @Value("${saml.idps.hoGent}") String hoGentIdp,
                                        @Value("${saml.idps.arteveldeHS}") String arteveldeHSIdp) {
        this.userDao = userDao;

        // Register IDP -> Institution mapping. IDP should match with the EntityID in the SAML metadata file.
        // Institution should match with the entry in the database.
        this.idpToInstitution = new HashMap<>();
        this.idpToInstitution.put(oktaIdp, Institution.ArteveldeHogeschool);
        this.idpToInstitution.put(ssoCircleIdp, Institution.HoGent);
        this.idpToInstitution.put(hoGentIdp, Institution.HoGent);
        this.idpToInstitution.put(arteveldeHSIdp, Institution.ArteveldeHogeschool);

        this.hoGentIdp = hoGentIdp;
        this.arteveldeHSIdp = arteveldeHSIdp;
    }


    @Override
    public User loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {

        // Try to find the user with given id (email) in the application database.
        User user;
        try {
            user = this.userDao.getUserByEmail(credential.getNameID().getValue());
        } catch (NoSuchDatabaseObjectException e) {
            // If it is the first time that this user is logging in, create a new user using the attributes defined by SAML.

            // Determine institution depending on the remote entity ID.
            if (!this.idpToInstitution.containsKey(credential.getRemoteEntityID())) {
                throw new InvalidRequestParametersException("User with email '" + credential.getNameID().getValue() + "' trying to login for the first time with unknown IDP '" + credential.getRemoteEntityID() + "'. Did an institution their EntityID change?");
            }
            String institution = this.idpToInstitution.get(credential.getRemoteEntityID());

            user = new User();
            user.setMail(credential.getNameID().getValue());
            user.setPassword("secret");
            user.setInstitution(institution);

            // Attributes are institution dependant so fill in firstname, lastname and ID depending on institution.
            if (credential.getRemoteEntityID().equals(hoGentIdp)) {
                user.setFirstName(credential.getAttributeAsString("first_name"));
                user.setLastName(credential.getAttributeAsString("last_name"));
                user.setUserId(credential.getAttributeAsString("user_name"));
            } else if (credential.getRemoteEntityID().equals(arteveldeHSIdp)) {
                user.setFirstName(credential.getAttributeAsString("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname"));
                user.setLastName(credential.getAttributeAsString("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname"));
                user.setUserId(credential.getAttributeAsString("http://schemas.microsoft.com/identity/claims/objectidentifier"));
            } else {
                user.setUserId(UUID.randomUUID().toString());
            }
            userDao.addUser(user);
        }
        return user;
    }
}
