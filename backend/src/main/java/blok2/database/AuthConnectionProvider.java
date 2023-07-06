package blok2.database;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Abstract class used for the token database. Implements universal functionalities.
 */
@Service
@ConfigurationProperties(prefix = "spring.auth-datasource")
public class AuthConnectionProvider {

    private String url;
    private String username;
    private String password;

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

}


