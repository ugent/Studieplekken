package be.ugent.blok2.config;

import be.ugent.blok2.helpers.Resources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
public class LdapConfig {
    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(Resources.applicationProperties.getString("ldap.url"));
        contextSource.setBase(Resources.applicationProperties.getString("ldap.base"));
        contextSource.setUserDn(Resources.applicationProperties.getString("ldap.userDn"));
        contextSource.setPassword(Resources.applicationProperties.getString("ldap.password"));
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(contextSource());
    }
}
