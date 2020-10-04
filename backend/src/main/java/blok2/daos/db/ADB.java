package blok2.daos.db;

import blok2.controllers.CalendarPeriodsForLockersController;
import org.flywaydb.core.Flyway;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class used for all the database DAOs. Implements universal functionalities.
 */
@Service
@ConfigurationProperties(prefix = "spring.datasource")
public class ADB {
    private String url;
    private String username;
    private String password;

    private final Logger logger = Logger.getLogger(CalendarPeriodsForLockersController.class.getSimpleName());

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
        logger.log(Level.INFO, String.format("Cleaning the database with credentials: %s, %s:%s", url, username, password));
        Flyway flyway = Flyway.configure().dataSource(url, username, password).load();
        flyway.clean();
    }

    public void createDatabase() {
        logger.log(Level.INFO, String.format("Creating a clean database with credentials: %s, %s:%s", url, username, password));
        Flyway flyway = Flyway.configure().dataSource(url, username, password).load();
        flyway.clean();
        flyway.migrate();
    }
}


