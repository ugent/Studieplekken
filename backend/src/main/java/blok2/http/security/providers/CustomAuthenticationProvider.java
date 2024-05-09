package blok2.http.security.providers;

import blok2.http.security.config.CustomAuthenticationToken;
import blok2.http.security.services.BackdoorUserDetailService;
import blok2.model.users.User;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class CustomAuthenticationProvider implements AuthenticationProvider {

    private BackdoorUserDetailService detailService;

    public CustomAuthenticationProvider(BackdoorUserDetailService detailService) {
        this.detailService = detailService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        User user = this.detailService.loadUserDetails((String) authentication.getPrincipal());
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
