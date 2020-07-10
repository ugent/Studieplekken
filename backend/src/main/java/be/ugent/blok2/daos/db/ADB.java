package be.ugent.blok2.daos.db;

import be.ugent.blok2.daos.IDao;
import be.ugent.blok2.helpers.Resources;
import org.apache.commons.codec.language.Soundex;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Abstract class used for all the database DAOs. Implements universal functionalities.
 */
public abstract class ADB implements IDao {

    protected static final ResourceBundle databaseProperties = Resources.databaseProperties;
    protected static final ResourceBundle applicationProperties = Resources.applicationProperties;
    protected Soundex soundex = new Soundex();

    private String connectionUrl;
    private String connectionUser;
    private String connectionPassword;

    public ADB() {
        useDefaultDatabaseConnection();
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionUrl, connectionUser, connectionPassword);
    }

    @Override
    public void setDatabaseConnectionUrl(String url) {
        connectionUrl = url;
    }

    @Override
    public void setDatabaseCredentials(String user, String password) {
        connectionUser = user;
        connectionPassword = password;
    }

    @Override
    public void useDefaultDatabaseConnection() {
        connectionUrl = applicationProperties.getString("db_url");
        connectionUser = applicationProperties.getString("db_user");
        connectionPassword = applicationProperties.getString("db_password");
    }
}
