package be.ugent.blok2.helpers.generators;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TestSessionMappingGenerator {

    private IGenerator<String> generator = new SessionMappingGenerator();

    //Check if promised randomness can be achieved.
    @Test
    public void testRandomness(){
        /*
        This generator promises at least 10 000 random string every ms so we
        generator 10 000 strings and check if there are duplicates. We only ask
        10 000 in case this test can run in 1 ms.
         */
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            strings.add(generator.generate());
        }

        for (String s: strings) {
            int frequency = Collections.frequency(strings, s);
            // no duplicates allowed
            assertEquals(frequency,1);
        }
    }
}
