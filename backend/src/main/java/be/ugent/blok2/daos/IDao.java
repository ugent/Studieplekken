package be.ugent.blok2.daos;

import java.util.Properties;

public interface IDao {
    /**
     * Let the DAO use a different connection URL than the one provided
     * in the property db_url in application.properties
     * Used in testing (for using the test database)
     */
    void setDatabaseConnectionUrl(String url);

    /**
     * Let the DAO use different connection credentials than the one provided
     * in the properties db_user and db_password in application.properties
     * Used in testing (for using the test database)
     */
    void setDatabaseCredentials(String user, String password);

    /**
     * Let the DAO use the default connection.
     */
    void useDefaultDatabaseConnection();
}
