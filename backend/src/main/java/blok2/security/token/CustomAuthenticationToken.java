package blok2.security.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class CustomAuthenticationToken extends AbstractAuthenticationToken {

    private final String username;

    public CustomAuthenticationToken(String username) {
        super(null);
        this.username = username;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }
}
