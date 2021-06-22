package blok2.daos.db;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Abstract class used for all the database DAOs. Implements universal functionalities.
 */
@Service
@ConfigurationProperties(prefix = "spring.datasource")
public class ADB {
    private String url;
    private String username;
    private String password;

    private final Logger logger = LoggerFactory.getLogger(ADB.class);

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void cleanDatabase() {
        logger.info(String.format("Cleaning the database with credentials: %s, %s:%s", url, username, password));
        Flyway flyway = Flyway.configure().dataSource(url, username, password).load();
        flyway.clean();
    }

    public void createDatabase() {
        logger.info(String.format("Creating a clean database with credentials: %s, %s:%s", url, username, password));
        Flyway flyway = Flyway.configure().dataSource(url, username, password).load();
        flyway.clean();
        flyway.migrate();
    }
}


