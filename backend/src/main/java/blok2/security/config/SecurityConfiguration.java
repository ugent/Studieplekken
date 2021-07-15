package blok2.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.*;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.web.*;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.*;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${server.port}")
    private int serverPort = 8080;

    @Value("${saml.sp}")
    private String samlAudience;

    @Value("${saml.entity-base-url}")
    private String entityBaseUrl;

    private final MetadataDisplayFilter metadataDisplayFilter;

    private final CasAuthenticationProvider casAuthenticationProvider;
    private final CasAuthenticationEntryPoint casAuthenticationEntryPoint;
    private final LogoutSuccessHandler logoutSuccessHandler;

    private final Set<String> springProfilesActive;

    private final SAMLAuthenticationProvider samlAuthenticationProvider;
    private SAMLEntryPoint samlEntryPoint;
    private final SAMLLogoutFilter samlLogoutFilter;
    private final SAMLLogoutProcessingFilter samlLogoutProcessingFilter;
    private final ExtendedMetadata extendedMetadata;
    private final KeyManager keyManager;

    @Qualifier("saml")
    private final SavedRequestAwareAuthenticationSuccessHandler samlAuthSuccessHandler;
    @Qualifier("saml")
    private final SimpleUrlAuthenticationFailureHandler samlAuthFailureHandler;


    @Autowired
    public SecurityConfiguration(CasAuthenticationProvider casAuthenticationProvider,
                                 CasAuthenticationEntryPoint casAuthenticationEntryPoint,
                                 @Qualifier("customLogoutSuccessHandler") LogoutSuccessHandler logoutSuccessHandler,
                                 Environment env,
                                 SAMLAuthenticationProvider samlAuthenticationProvider,
                                 SAMLLogoutFilter samlLogoutFilter,
                                 SAMLLogoutProcessingFilter samlLogoutProcessingFilter,
                                 @Qualifier("successRedirectHandler") SavedRequestAwareAuthenticationSuccessHandler samlAuthSuccessHandler,
                                 SimpleUrlAuthenticationFailureHandler samlAuthFailureHandler,
                                 ExtendedMetadata extendedMetadata,
                                 KeyManager keyManager,
                                 MetadataDisplayFilter metadataDisplayFilter) {
        this.casAuthenticationProvider = casAuthenticationProvider;
        this.casAuthenticationEntryPoint = casAuthenticationEntryPoint;
        this.logoutSuccessHandler = logoutSuccessHandler;

        this.springProfilesActive = new TreeSet<>();
        Collections.addAll(springProfilesActive, env.getActiveProfiles());

        this.samlAuthenticationProvider = samlAuthenticationProvider;

        // These endpoints are not actually being used, but are implemented for completeness.
        // Logout is universally handled by the CustomLogoutSuccessHandler which overwrites the cookies to logout.
        this.samlLogoutFilter = samlLogoutFilter;
        this.samlLogoutFilter.setFilterProcessesUrl("/logout/saml");
        this.samlLogoutProcessingFilter = samlLogoutProcessingFilter;
        this.samlLogoutProcessingFilter.setFilterProcessesUrl("/SingleLogout/saml");

        this.samlAuthSuccessHandler = samlAuthSuccessHandler;
        this.samlAuthFailureHandler = samlAuthFailureHandler;
        this.extendedMetadata = extendedMetadata;
        this.keyManager = keyManager;

        this.metadataDisplayFilter = metadataDisplayFilter;
        this.metadataDisplayFilter.setFilterProcessesUrl("/metadata/saml");
    }

    @Autowired
    public void setSamlEntryPoint(SAMLEntryPoint samlEntryPoint) {
        // A setter is used instead of initialising it via the constructor to prevent circular dependency.
        this.samlEntryPoint = samlEntryPoint;
        this.samlEntryPoint.setFilterProcessesUrl("/login/saml");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(this.casAuthenticationProvider);
        auth.authenticationProvider(this.samlAuthenticationProvider);
    }

    /**
     * Disable the Spring Security chain all together for the endpoints "/dev/*"
     * if the spring.profiles.active environment variable contains "dev".
     */
    @Override
    public void configure(WebSecurity webSecurity) {
        if (springProfilesActive.contains("dev"))
            webSecurity.ignoring().antMatchers("/dev/**");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .disable();
        //.csrfTokenRepository(csrfTokenRepository());

        http.authorizeRequests()
                .regexMatchers("/login/cas", "/login/saml", "/SSO/saml", "/discovery/saml").authenticated()// used to trigger cas flow
                .anyRequest().permitAll();

        http.exceptionHandling()
                .defaultAuthenticationEntryPointFor(
                        casAuthenticationEntryPoint,
                        new AntPathRequestMatcher("/login/cas"))
                .defaultAuthenticationEntryPointFor(
                        samlEntryPoint,
                        new AntPathRequestMatcher("/login/saml"));
        /*http
                .httpBasic()
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().endsWith("/saml")) {
                        samlEntryPoint.commence(request, response, authException);
                        System.out.println("here 1: " + request.getRequestURI());
                    } else if (request.getRequestURI().endsWith("/login/cas")){
                        casAuthenticationEntryPoint.commence(request, response, authException);
                        System.out.println("here 2: " + request.getRequestURI());
                    } else {
                        System.out.println("here 3: " + request.getRequestURI());
                    }
                });*/

        http
                .addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class)
                .addFilterAfter(samlFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(samlFilter(), CsrfFilter.class);

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

    @Bean
    public FilterChainProxy samlFilter() throws Exception {
        List<SecurityFilterChain> chains = new ArrayList<>();
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/SSO/saml/**"),
                samlWebSSOProcessingFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/discovery/saml/**"),
                samlDiscovery()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/login/saml/**"),
                samlEntryPoint));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/logout/saml/**"),
                samlLogoutFilter));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/SingleLogout/saml/**"),
                samlLogoutProcessingFilter));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/metadata/saml/**"),
                metadataDisplayFilter));
        return new FilterChainProxy(chains);
    }

    /**
     * Will authenticate the associated auth token when the user logs in and the IdP redirects the SAML response to the
     * /SSO/saml URI for processing.
     */
    @Bean
    public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
        SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
        samlWebSSOProcessingFilter.setFilterProcessesUrl("/SSO/saml");
        samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager());
        samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(samlAuthSuccessHandler);
        samlWebSSOProcessingFilter.setAuthenticationFailureHandler(samlAuthFailureHandler);
        return samlWebSSOProcessingFilter;
    }

    /**
     * Will discover the IdP to contact for authentication after that samlEntryPoint handled the entry request.
     */
    @Bean
    public SAMLDiscovery samlDiscovery() {
        SAMLDiscovery samlDiscovery = new SAMLDiscovery();
        samlDiscovery.setFilterProcessesUrl("/discovery/saml");
        return samlDiscovery;
    }

    public MetadataGenerator metadataGenerator() throws Exception {
        MetadataGenerator metadataGenerator = new MetadataGenerator();
        metadataGenerator.setEntityId(samlAudience);
        metadataGenerator.setExtendedMetadata(extendedMetadata);
        metadataGenerator.setIncludeDiscoveryExtension(false);
        metadataGenerator.setKeyManager(keyManager);
        metadataGenerator.setSamlWebSSOFilter(samlWebSSOProcessingFilter());
        metadataGenerator.setSamlLogoutProcessingFilter(samlLogoutProcessingFilter);
        metadataGenerator.setEntityBaseURL(entityBaseUrl);
        return metadataGenerator;
    }

    @Bean
    public MetadataGeneratorFilter metadataGeneratorFilter() throws Exception {
        return new MetadataGeneratorFilter(metadataGenerator());
    }
}
