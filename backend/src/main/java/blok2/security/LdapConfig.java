package blok2.security;

import blok2.helpers.Resources;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
@ConfigurationProperties(prefix = "ldap")
public class LdapConfig {

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

    public void setUrl(String url) {
        this.url = url;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public void setUserDn(String userDn) {
        this.userDn = userDn;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
