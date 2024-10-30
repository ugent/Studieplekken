package blok2.ldap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "ldap")
@Setter
public class LdapConfiguration {
    private String url;
    private String base;
    private String userDn;
    private String password;

    @Bean
    public LdapContextSource ldapContextSource() {
        LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl(url);
        ldapContextSource.setBase(base);
        ldapContextSource.setUserDn(userDn);
        ldapContextSource.setPassword(password);
        return ldapContextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(ldapContextSource());
    }
}
