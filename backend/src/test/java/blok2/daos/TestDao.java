package blok2.daos;

import blok2.daos.db.ADB;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;

@SpringBootTest
@RunWith(SpringRunner.class)
public abstract class TestDao {

    @Autowired
    protected ADB adb;

    /**
     * Wil be ran before every test to provide a fresh populated database
     * @throws SQLException
     */
    public abstract void populateDatabase() throws SQLException;

    @Before
    public void setup() throws SQLException {
        adb.createSchema();
        populateDatabase();
    }

    @After
    public void cleanup() throws SQLException {
        adb.dropSchema();
    }
}
