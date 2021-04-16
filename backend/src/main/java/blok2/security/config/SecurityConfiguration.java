package blok2.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.PortMapper;
import org.springframework.security.web.PortMapperImpl;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${server.port}")
    private int serverPort = 8080;

    private final CasAuthenticationProvider casAuthenticationProvider;
    private final CasAuthenticationEntryPoint casAuthenticationEntryPoint;
    private final LogoutSuccessHandler logoutSuccessHandler;

    private final Set<String> springProfilesActive;

    @Autowired
    public SecurityConfiguration(CasAuthenticationProvider casAuthenticationProvider,
                                 CasAuthenticationEntryPoint casAuthenticationEntryPoint,
                                 LogoutSuccessHandler logoutSuccessHandler,
                                 Environment env) {
        this.casAuthenticationProvider = casAuthenticationProvider;
        this.casAuthenticationEntryPoint = casAuthenticationEntryPoint;
        this.logoutSuccessHandler = logoutSuccessHandler;

        this.springProfilesActive = new TreeSet<>();
        Collections.addAll(springProfilesActive, env.getActiveProfiles());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(this.casAuthenticationProvider);
    }

    /**
     * Disable the Spring Security chain all together for the endpoints "/dev/*"
     * if the spring.profiles.active environment variable contains "dev".
     */
    @Override
    public void configure(WebSecurity webSecurity) {
        if (springProfilesActive.contains("dev"))
            webSecurity.ignoring().antMatchers("/dev/*");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .csrfTokenRepository(csrfTokenRepository());

        http.authorizeRequests()
                .regexMatchers("/login/cas").authenticated() // used to trigger cas flow
                .anyRequest().permitAll();

        http.httpBasic()
                .authenticationEntryPoint(casAuthenticationEntryPoint);

        http.requestCache().requestCache(requestCache());

        http.logout()
                .logoutSuccessHandler(logoutSuccessHandler)
                .logoutUrl("/logout");
    }

    /**
     * The class PortMapperImpl will put two values by default in
     * its attribute 'httpsPortMappings':
     * - 80 -> 443
     * - 8080 -> 8443
     * <p>
     * This is a problem when the AbstractAuthenticationProcessingFilter
     * calls successHandler.onAuthenticationSuccess(...) in the method
     * AbstractAuthenticationProcessingFilter#successfulAuthentication(...).
     * <p>
     * (Note: the successHandler mentioned above by default is an instance of
     * SavedRequestAwareAuthenticationSuccessHandler)
     * <p>
     * After validating the ticket, the server needs to redirect the user's
     * browser to the original request-url. But thanks to the default
     * PortMapperImpl, the port 8080 will be redirected to 8443, which is
     * not correct. Thanks to https://stackoverflow.com/a/55660281/9356123,
     * this is a working implementation.
     */
    private PortMapper portMapper() {
        PortMapperImpl portMapper = new PortMapperImpl();

        if (serverPort == -1) {
            serverPort = 8080;
        }

        portMapper.setPortMappings(
            new HashMap<String, String>() {{
                put(Integer.toString(serverPort), Integer.toString(serverPort));
            }}
        );

        return portMapper;
    }

    private RequestCache requestCache() {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        PortResolverImpl portResolver = new PortResolverImpl();
        portResolver.setPortMapper(portMapper());
        requestCache.setPortResolver(portResolver);
        return requestCache;
    }

    private CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokenRepository.setCookiePath("/");
        return tokenRepository;
    }
}
