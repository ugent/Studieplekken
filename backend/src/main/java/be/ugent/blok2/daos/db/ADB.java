package be.ugent.blok2.daos.db;

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
public abstract class ADB {

    protected final ResourceBundle resourceBundle = Resources.applicationProperties;
    protected Soundex soundex = new Soundex();

    protected Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", resourceBundle.getString("user"));
        props.setProperty("password", resourceBundle.getString("pass"));
        return DriverManager.getConnection(resourceBundle.getString("URL"), props);
    }
}
