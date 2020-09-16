package blok2.security;

import blok2.helpers.Resources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
public class LdapConfig {

    @Bean
    public LdapContextSource ldapContextSource() {
        LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl(Resources.blokatugentConf.getString("ldap.url"));
        ldapContextSource.setBase(Resources.blokatugentConf.getString("ldap.base"));
        ldapContextSource.setUserDn(Resources.blokatugentConf.getString("ldap.userDn"));
        ldapContextSource.setPassword(Resources.blokatugentConf.getString("ldap.password"));
        return ldapContextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(ldapContextSource());
    }

}
