package blok2;

import blok2.daos.db.ConnectionProvider;
import config.CustomFlywayConfig;
import org.flywaydb.test.FlywayTestExecutionListener;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.sql.SQLException;

@Import(CustomFlywayConfig.class)
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        FlywayTestExecutionListener.class
})
public abstract class BaseTest {

    @Autowired
    protected ConnectionProvider connectionProvider;

    /**
     * Wil be ran before every test to provide a fresh populated database
     */
    public abstract void populateDatabase() throws SQLException;

    @Before
    @FlywayTest // Executes for a class or for a method. Combining with 'before' executes before every test.
    public void setup() throws SQLException {
        System.out.println("populating");
        populateDatabase();
    }

    @After
    public void cleanup() {}

}
