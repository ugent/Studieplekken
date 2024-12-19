package blok2.security;

import blok2.security.filter.CustomAuthenticationFilter;
import blok2.security.filter.JwtAuthenticationFilter;
import blok2.security.provider.CustomAuthenticationProvider;
import blok2.security.provider.JwtAuthenticationProvider;
import blok2.security.services.BackdoorUserDetailService;
import blok2.security.services.JwtService;
import blok2.security.services.JwtUserDetailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import java.util.*;

@Configuration
@EnableWebSecurity
@EnableJpaAuditing
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${server.port}")
    private int serverPort = 8080;

    @Value("${saml.sp}")
    private String samlAudience;

    @Value("${saml.entity-base-url}")
    private String entityBaseUrl;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private BackdoorUserDetailService backdoorDetailService;

    @Autowired
    private JwtUserDetailService jwtUserDetailService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private  Environment environment;

    private LogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    public SecurityConfiguration(@Qualifier("customLogoutSuccessHandler") LogoutSuccessHandler logoutSuccessHandler) {
        this.logoutSuccessHandler = logoutSuccessHandler;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new CustomAuthenticationProvider(backdoorDetailService));
        auth.authenticationProvider(new JwtAuthenticationProvider(jwtUserDetailService, jwtService));
    }

    /**
     * Disable the Spring Security chain all together for the endpoints "/dev/*"
     * if the spring.profiles.active environment variable contains "dev".
     */
    @Override
    public void configure(WebSecurity webSecurity) {
        if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            webSecurity.ignoring().antMatchers("/dev/**");
        }
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // Register the csrf token repository.
        http.csrf().csrfTokenRepository(csrfTokenRepository());

        // Register the logout flow.
        http.logout().logoutSuccessHandler(logoutSuccessHandler).logoutUrl("/logout");

        // Add the JWT authentication filter.
        http.addFilterAfter(new JwtAuthenticationFilter(authManager), BasicAuthenticationFilter.class);

        // Add the AS-USER authentication filter.
        Collection<String> env = Arrays.asList(environment.getActiveProfiles());

        if (env.contains("dev")) {
            http.addFilterAfter(new CustomAuthenticationFilter(authManager), JwtAuthenticationFilter.class);
        }

        // Disable CSRF tokens.
        // TODO(ewverlin): why is this disabled? This is a security risk.
        http.csrf().disable();
    }

    private CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokenRepository.setCookiePath("/");
        return tokenRepository;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
