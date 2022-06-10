package blok2.controllers;

import blok2.security.services.CustomUserDetailsService;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * RestController class which provides convenience endpoints that are only available in development
 * (that is, if the environment variable spring.profiles.active contains "dev").
 * For easy access, the Spring Security Chain is disabled for all endpoints "/dev/*" in development
 * (cfr. SecurityConfiguration.configure(WebSecurity)). Therefore, one can use e.g. Postman without
 * authentication to access each of the available convenience endpoints.
 */
@RestController
@Profile("dev")
@RequestMapping("dev")
public class DevelopmentController {

    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public DevelopmentController(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Provide an endpoint in development to create a new user given the userId.
     * <p>
     * This is useful to easily track down problems that occur with certain users
     * in the CustomUserDetailsService while they try to log in for the first time
     * in production, but some error occurs. Just put a breakpoint at beginning of
     * the method CustomUserDetailsService.loadUserDetails() and walk through the
     * process of creating a that user and try to figure out where the problem occurs.
     */
    @PostMapping("/users/{userId}")
    public void createUser(@PathVariable("userId") String userId) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("mail", "not.important@example.com");
        attributes.put("ugentID", userId);

        AttributePrincipal principal = new AttributePrincipalImpl("dummy", attributes);
        Assertion assertion = new AssertionImpl(principal, attributes);
        CasAssertionAuthenticationToken authenticationToken =
                new CasAssertionAuthenticationToken(assertion, "no-ticket");

        customUserDetailsService.loadUserDetails(authenticationToken);
    }


}
