package blok2.daos.db;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@ConfigurationProperties(prefix = "scripts")
public class DatabaseScriptsConfiguration {
    private String dropSchema;
    private String createSchema;

    public void setDropSchema(String dropSchema) {
        this.dropSchema = dropSchema;
    }

    public void setCreateSchema(String createSchema) {
        this.createSchema = createSchema;
    }

    public String getDropSchema() {
        return dropSchema;
    }

    public String getCreateSchema() {
        return createSchema;
    }
}
