package be.ugent.blok2.configuration;

import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Arrays;

@TestConfiguration
public class SecurityConfig {

    public static User admin = new User("0000000006002", "admin", "admin"
            , "admin", "admin", "UGent"
            ,  new Role[]{Role.EMPLOYEE, Role.ADMIN}, -1,"001703195697");

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(Arrays.asList(
                admin
        ));
    }
}
