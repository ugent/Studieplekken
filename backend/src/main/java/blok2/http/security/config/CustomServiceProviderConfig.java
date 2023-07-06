package blok2.security.config;

import com.github.ulisesbocchio.spring.boot.security.saml.configurer.ServiceProviderBuilder;
import com.github.ulisesbocchio.spring.boot.security.saml.configurer.ServiceProviderConfigurerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomServiceProviderConfig extends ServiceProviderConfigurerAdapter {

    @Value("${saml.sp-metadata}")
    private String samlSPMetadata;

    @Override
    public void configure(ServiceProviderBuilder serviceProvider) {
        serviceProvider.metadataManager().localMetadataLocation(this.samlSPMetadata).refreshCheckInterval(0);
    }
}
