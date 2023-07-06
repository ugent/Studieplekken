package blok2.http.security.providers;

import blok2.model.users.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLCredential;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CustomSAMLAuthenticationProvider extends SAMLAuthenticationProvider {

    public CustomSAMLAuthenticationProvider() {
        super();
        this.setForcePrincipalAsString(false);
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        return super.authenticate(authentication);
    }

    @Override
    public Collection<? extends GrantedAuthority> getEntitlements(SAMLCredential credential, Object userDetail) {
        if (userDetail instanceof ExpiringUsernameAuthenticationToken) {
            return new ArrayList<>(((ExpiringUsernameAuthenticationToken) userDetail).getAuthorities());
        } else if (userDetail instanceof User) {
            User user = (User) userDetail;
            return user.getAuthorities();
        } else {
            return Collections.emptyList();
        }
    }
}
