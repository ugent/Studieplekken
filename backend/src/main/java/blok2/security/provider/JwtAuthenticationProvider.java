package blok2.security.provider;

import blok2.model.users.User;
import blok2.security.services.JwtService;
import blok2.security.services.JwtUserDetailService;
import blok2.security.token.JwtAuthenticationToken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ResponseStatusException;

public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtUserDetailService detailService;
    private final JwtService jwtService;

    @Autowired
    public JwtAuthenticationProvider(JwtUserDetailService detailService, JwtService jwtService) {
        this.detailService = detailService;
        this.jwtService = jwtService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        String token = (String) jwtAuthenticationToken.getPrincipal();
        if(this.jwtService.isValid(token)) {
            User user = this.detailService.loadUserDetails(token);
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        }
        else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
