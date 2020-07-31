package be.ugent.blok2.security;

import be.ugent.blok2.helpers.Resources;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.security.AuthenticationProviders.BlokAtUGentAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableGlobalMethodSecurity(jsr250Enabled = true)
public class AuthenticationConfiguration
        extends WebSecurityConfigurerAdapter {

    private final BlokAtUGentAuthenticationProvider blokAtUGentAuthenticationProvider;
    private final CasAuthenticationProvider casAuthenticationProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    public AuthenticationConfiguration(BlokAtUGentAuthenticationProvider blokAtUGentAuthenticationProvider
            , CasAuthenticationProvider casAuthenticationProvider
            , CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.blokAtUGentAuthenticationProvider = blokAtUGentAuthenticationProvider;
        this.casAuthenticationProvider = casAuthenticationProvider;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    @Override
    protected void configure(HttpSecurity http)
            throws Exception {
        http.csrf().ignoringAntMatchers("/signout")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .authorizeRequests()
                .mvcMatchers("/cas").hasAnyAuthority("STUDENT")  // triggers the cas login page
                .antMatchers("/**").permitAll()
                .mvcMatchers("/actuator/**").hasAnyAuthority("ADMIN")
                .anyRequest().authenticated()
                .and()
                .headers().frameOptions().disable()
                .and()
                .exceptionHandling()
                //.authenticationEntryPoint(customAuthenticationEntryPoint)
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .and()
                .formLogin()
                .loginProcessingUrl("/signin").permitAll()  // form to login with own login-system
                .loginPage("/login").permitAll()
                .usernameParameter("mail")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard")
                .failureUrl("/fail").successHandler(new AuthSuccessHandler())
                .and()
                .logout().logoutSuccessHandler(new LogoutSuccesHandler())
                .logoutUrl("/signout");
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers("/api/account/new") //A person who isn't logged in must be able to post to this url to register
                .antMatchers("/favicon.ico");
    }

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() {
        // all sources that can verify if a user is logging in with the correct credentials
        List<AuthenticationProvider> list = new ArrayList<>();
        list.add(casAuthenticationProvider);
        list.add(blokAtUGentAuthenticationProvider);
        return new ProviderManager(list);
    }

    @Bean
    public CasAuthenticationFilter casAuthenticationFilter(
            AuthenticationManager authenticationManager,
            ServiceProperties serviceProperties) {
        CasAuthenticationFilter filter = new CasAuthenticationFilter();
        filter.setFilterProcessesUrl("/process"); // send ticket to this url
        filter.setAuthenticationManager(authenticationManager);
        filter.setServiceProperties(serviceProperties);
        filter.setAuthenticationSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
            // set mapping cookie and redirect to the dashboard
            Object principal = authentication.getPrincipal();
            String mapping = UsersCache.getInstance().login(httpServletRequest.getSession(false).getId(), (User) principal);
            Cookie cookie = new Cookie("mapping", mapping);
            cookie.setHttpOnly(false);
            httpServletResponse.addCookie(cookie);
            httpServletResponse.sendRedirect(Resources.applicationProperties.getString("urlBase") + "/dashboard");
        });
        return filter;
    }
}
