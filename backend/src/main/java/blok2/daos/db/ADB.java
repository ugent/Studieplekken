package blok2.daos.db;

import blok2.daos.IDao;
import blok2.helpers.Resources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Abstract class used for all the database DAOs. Implements universal functionalities.
 */
public abstract class ADB implements IDao {

    protected static final ResourceBundle databaseProperties = Resources.databaseProperties;
    protected static final ResourceBundle applicationProperties = Resources.applicationProperties;

    private static String connectionUrl;
    private static String connectionUser;
    private static String connectionPassword;

    public ADB() {
        useDefaultDatabaseConnection();
    }

    public static Connection getConnection() throws SQLException {
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
