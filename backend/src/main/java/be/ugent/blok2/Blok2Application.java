package be.ugent.blok2;

import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.helpers.Resources;
import be.ugent.blok2.security.UserDetailsServices.CasUserDetailsService;
import be.ugent.blok2.services.LdapService;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.web.client.*;

import javax.naming.Context;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLClassLoader;

@ServletComponentScan
@SpringBootApplication
public class Blok2Application extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(Blok2Application.class, args);
    }

    @Bean
    public HttpMessageConverter<BufferedImage> createImageHttpMessageConverter() {
        return new BufferedImageHttpMessageConverter();
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3 * 1000);
        factory.setReadTimeout(7 * 1000);
        return factory;
    }

    @Bean
    public RestOperations restTemplate() {
        RestTemplate restTemplate = new RestTemplate(this.clientHttpRequestFactory());
        return restTemplate;
    }

    @Bean
    public ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(Resources.applicationProperties.getString("hostUrl")
                + Resources.applicationProperties.getString("urlBase") + "/process");
        serviceProperties.setSendRenew(false);
        return serviceProperties;
    }

    @Bean
    public CasAuthenticationProvider casAuthenticationProvider(
            TicketValidator ticketValidator,
            ServiceProperties serviceProperties,
            IAccountDao accountDao,
            LdapService ldapService) {
        CasAuthenticationProvider provider = new CasAuthenticationProvider();
        provider.setServiceProperties(serviceProperties);
        provider.setTicketValidator(ticketValidator);
        provider.setAuthenticationUserDetailsService(new CasUserDetailsService(accountDao, ldapService));
        provider.setKey("CAS_PROVIDER_LOCALHOST_8080");
        return provider;
    }

    @Bean
    public TicketValidator ticketValidator() {
        return new Cas20ServiceTicketValidator(Resources.applicationProperties.getString("casLoginUrl"));
    }
}
