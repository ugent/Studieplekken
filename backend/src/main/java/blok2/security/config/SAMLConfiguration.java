package blok2.security.config;

import blok2.security.providers.CustomSAMLAuthenticationProvider;
import blok2.security.providers.InputStreamMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.saml.*;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.context.SAMLContextProviderLB;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.HTTPPostBinding;
import org.springframework.security.saml.processor.HTTPRedirectDeflateBinding;
import org.springframework.security.saml.processor.SAMLBinding;
import org.springframework.security.saml.processor.SAMLProcessorImpl;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.*;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*******************************************************************************************
 *                  Beans for SAML Client Single Sign-On Configuration                     *
 *                                                                                         *
 *    For a more detailed explanation, have a look at                                      *
 *      - https://www.baeldung.com/spring-security-saml                                    *
 *******************************************************************************************/
@Configuration
public class SAMLConfiguration {

    @Value("${saml.keystore.location}")
    private String samlKeystoreLocation;

    @Value("${saml.keystore.password}")
    private String samlKeystorePassword;

    @Value("${saml.keystore.alias}")
    private String samlKeystoreAlias;

    @Value("${saml.idp}")
    private String defaultIdp;

    @Value("${redirect.successLoginRedirectUrl}")
    private String successLoginRedirectUrl;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${saml.server-name}")
    private String serverName;


    @Bean
    public WebSSOProfile webSSOprofile() {
        return new WebSSOProfileImpl();
    }

    @Bean
    public WebSSOProfileOptions defaultWebSSOProfileOptions() {
        WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
        webSSOProfileOptions.setIncludeScoping(false);
        return webSSOProfileOptions;
    }

    /**
     * Entry point for authentication.
     * Will handle the request when user tries to log in for the first time.
     */
    @Bean
    public SAMLEntryPoint samlEntryPoint() {
        SAMLEntryPoint samlEntryPoint = new SAMLEntryPoint();
        samlEntryPoint.setDefaultProfileOptions(defaultWebSSOProfileOptions());
        return samlEntryPoint;
    }

    @Bean
    public ExtendedMetadata extendedMetadata() {
        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setIdpDiscoveryEnabled(false);
        extendedMetadata.setSignMetadata(false);
        return extendedMetadata;
    }

    @Bean
    public KeyManager keyManager() {
        DefaultResourceLoader loader = new DefaultResourceLoader();
        Resource storeFile = loader.getResource(samlKeystoreLocation);
        Map<String, String> passwords = new HashMap<>();
        passwords.put(samlKeystoreAlias, samlKeystorePassword);
        return new JKSKeyManager(storeFile, samlKeystorePassword, passwords, samlKeystoreAlias);
    }

    @Bean
    @Qualifier("okta")
    public ExtendedMetadataDelegate oktaExtendedMetadataProvider() {
        InputStream metadata = null;
        try {
            metadata = new ClassPathResource("saml/metadata/sso-okta.xml").getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        InputStreamMetadataProvider provider = new InputStreamMetadataProvider(metadata);
        provider.setParserPool(parserPool());
        return new ExtendedMetadataDelegate(provider, extendedMetadata());
    }

    @Bean
    @Qualifier("ssoCircle")
    public ExtendedMetadataDelegate ssoCircleExtendedMetadataProvider() {
        InputStream metadata = null;
        try {
            metadata = new ClassPathResource("saml/metadata/sso-ssocircle.xml").getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        InputStreamMetadataProvider provider = new InputStreamMetadataProvider(metadata);
        provider.setParserPool(parserPool());
        return new ExtendedMetadataDelegate(provider, extendedMetadata());
    }

    @Bean
    @Qualifier("hoGent")
    public ExtendedMetadataDelegate hoGentExtendedMetadataProvider() {
        InputStream metadata = null;
        try {
            metadata = new ClassPathResource("saml/metadata/sso-hogent.xml").getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        InputStreamMetadataProvider provider = new InputStreamMetadataProvider(metadata);
        provider.setParserPool(parserPool());
        return new ExtendedMetadataDelegate(provider, extendedMetadata());
    }

    @Bean
    @Qualifier("arteveldeHS")
    public ExtendedMetadataDelegate arteveldeHSExtendedMetadataProvider() {
        InputStream metadata = null;
        try {
            metadata = new ClassPathResource("saml/metadata/sso-artevelde.xml").getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        InputStreamMetadataProvider provider = new InputStreamMetadataProvider(metadata);
        provider.setParserPool(parserPool());
        return new ExtendedMetadataDelegate(provider, extendedMetadata());
    }

    @Bean
    @Qualifier("metadata")
    public CachingMetadataManager metadata() throws MetadataProviderException {
        List<MetadataProvider> providers = new ArrayList<>();
        providers.add(oktaExtendedMetadataProvider());
        providers.add(ssoCircleExtendedMetadataProvider());
        providers.add(hoGentExtendedMetadataProvider());
        providers.add(arteveldeHSExtendedMetadataProvider());
        CachingMetadataManager metadataManager = new CachingMetadataManager(providers);
        metadataManager.setDefaultIDP(defaultIdp);
        return metadataManager;
    }

    @Bean(initMethod = "initialize")
    public StaticBasicParserPool parserPool() {
        return new StaticBasicParserPool();
    }

    @Bean(name = "parserPoolHolder")
    public ParserPoolHolder parserPoolHolder() {
        return new ParserPoolHolder();
    }

    @Bean
    public HTTPPostBinding httpPostBinding() {
        return new HTTPPostBinding(parserPool(), VelocityFactory.getEngine());
    }

    @Bean
    public HTTPRedirectDeflateBinding httpRedirectDeflateBinding() {
        return new HTTPRedirectDeflateBinding(parserPool());
    }

    @Bean
    public SAMLProcessorImpl processor() {
        ArrayList<SAMLBinding> bindings = new ArrayList<>();
        bindings.add(httpRedirectDeflateBinding());
        bindings.add(httpPostBinding());
        return new SAMLProcessorImpl(bindings);
    }

    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        return new CustomSAMLAuthenticationProvider();
    }

    @Bean
    public SAMLContextProviderImpl contextProvider() {
        SAMLContextProviderLB samlContextProviderLB = new SAMLContextProviderLB();
        samlContextProviderLB.setScheme("https");
        samlContextProviderLB.setServerName(serverName);
        samlContextProviderLB.setContextPath(contextPath);
        return samlContextProviderLB;
    }

    @Bean
    public static SAMLBootstrap samlBootstrap() {
        return new SAMLBootstrap();
    }

    @Bean
    public SAMLDefaultLogger samlLogger() {
        return new SAMLDefaultLogger();
    }

    @Bean
    public WebSSOProfileConsumer webSSOprofileConsumer() {
        return new WebSSOProfileConsumerImpl();
    }

    @Bean
    @Qualifier("hokWebSSOprofileConsumer")
    public WebSSOProfileConsumerHoKImpl hokWebSSOProfileConsumer() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    @Bean
    public WebSSOProfileECPImpl ecpProfile() {
        return new WebSSOProfileECPImpl();
    }

    @Bean
    public SingleLogoutProfile logoutProfile() {
        return new SingleLogoutProfileImpl();
    }

    /**
     * Will redirect the user to the default target URL when login was successful.
     */
    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
        SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successRedirectHandler.setDefaultTargetUrl(this.successLoginRedirectUrl);
        return successRedirectHandler;
    }

    /**
     * Will redirect the user to the failure URL when login was not successful.
     */
    @Bean
    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
        failureHandler.setUseForward(true);
        failureHandler.setDefaultFailureUrl("/error");
        return failureHandler;
    }

    // denk dat dit niet langer nodig is, eerdere success logout handler kan dit ook afhandelen.
    @Bean
    public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
        SimpleUrlLogoutSuccessHandler successLogoutHandler = new SimpleUrlLogoutSuccessHandler();
        successLogoutHandler.setDefaultTargetUrl("/welcome");
        return successLogoutHandler;
    }

    @Bean
    public SecurityContextLogoutHandler logoutHandler() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);
        return logoutHandler;
    }

    @Bean
    public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
        return new SAMLLogoutProcessingFilter(successLogoutHandler(), logoutHandler());
    }

    @Bean
    public SAMLLogoutFilter samlLogoutFilter() {
        return new SAMLLogoutFilter(successLogoutHandler(),
                new LogoutHandler[]{logoutHandler()},
                new LogoutHandler[]{logoutHandler()});
    }

    @Bean
    public MetadataDisplayFilter metadataDisplayFilter() {
        return new MetadataDisplayFilter();
    }
}
