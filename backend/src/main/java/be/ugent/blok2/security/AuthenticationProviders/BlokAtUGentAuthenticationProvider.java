package be.ugent.blok2.security.AuthenticationProviders;

import be.ugent.blok2.security.UserDetailsServices.BlokAtUGentUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * This is a convenience class configuring DaoAuthenticationProvider in the
 * constructor as needed by the application.
 */
@Service
public class BlokAtUGentAuthenticationProvider extends DaoAuthenticationProvider {
    @Autowired
    public BlokAtUGentAuthenticationProvider(BlokAtUGentUserDetailsService blokAtUGentUserDetailsService) {
        this.setUserDetailsService(blokAtUGentUserDetailsService);
        this.setPasswordEncoder(new BCryptPasswordEncoder());
    }
}
