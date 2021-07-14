package blok2.security.providers;

import org.opensaml.saml2.metadata.provider.AbstractReloadingMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;

import java.io.InputStream;
import java.util.Timer;

/**
 * Metadata Provider from an InputStream.
 * <p>
 * Original Source from: https://github.com/OhadR/spring-oAuth2-SAML-integration/blob/master/src/main/java/com/ohadr/saml/InputStreamMetadataProvider.java
 */
public class InputStreamMetadataProvider extends AbstractReloadingMetadataProvider {
    /**
     * The metadata stream.
     */
    private final InputStream metadataInputStream;

    /**
     * Constructor.
     *
     * @param metadata the metadata stream
     */
    public InputStreamMetadataProvider(InputStream metadata) {
        super();
        metadataInputStream = metadata;
    }

    /**
     * Constructor.
     *
     * @param metadata            the metadata stream
     * @param backgroundTaskTimer timer used to refresh metadata in the background
     */
    public InputStreamMetadataProvider(Timer backgroundTaskTimer, InputStream metadata) {
        super(backgroundTaskTimer);
        metadataInputStream = metadata;
    }

    @Override
    protected String getMetadataIdentifier() {
        return toString();
    }

    @Override
    protected byte[] fetchMetadata() throws MetadataProviderException {
        return inputstreamToByteArray(metadataInputStream);
    }

}