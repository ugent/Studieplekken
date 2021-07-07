package blok2.security.providers;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLCredential;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CustomSAMLAuthenticationProvider extends SAMLAuthenticationProvider {

    @Override
    public Collection<? extends GrantedAuthority> getEntitlements(SAMLCredential credential, Object userDetail) {
        if (userDetail instanceof ExpiringUsernameAuthenticationToken) {
            return new ArrayList<>(((ExpiringUsernameAuthenticationToken) userDetail).getAuthorities());
        } else {
            return Collections.emptyList();
        }
    }
}
