package blok2.daos;

import blok2.daos.db.ADB;
import org.flywaydb.test.FlywayTestExecutionListener;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.sql.SQLException;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        FlywayTestExecutionListener.class
})
public abstract class TestDao {

    @Autowired
    protected ADB adb;

    /**
     * Wil be ran before every test to provide a fresh populated database
     * @throws SQLException
     */
    public abstract void populateDatabase() throws SQLException;

    @Before
    @FlywayTest // Executes for a class or for a method. Combining with 'before' executes before every test.
    public void setup() throws SQLException {
//        adb.createDatabase();
        populateDatabase();
    }

    @After
    public void cleanup() throws SQLException {
//        adb.cleanDatabase();
    }
}
