package blok2.http.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(
    prePostEnabled = true
)
public class MethodSecuritySecurity extends GlobalMethodSecurityConfiguration {
}
