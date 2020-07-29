package be.ugent.blok2.daos.cascade;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"db", "test"})
public class TestCascadeInDBLocationDao {

    @Before
    public void setup() {
        // Use test database

        // Setup test objects

        // Add test objects to database

    }

    @After
    public void cleanup() {
        // Remove test objects from database

        // Use regular database

    }

    @Test
    public void doNothing() {

    }
}
