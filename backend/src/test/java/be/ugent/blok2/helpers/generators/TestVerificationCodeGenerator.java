package be.ugent.blok2.helpers.generators;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TestVerificationCodeGenerator {
    private IGenerator<String> generator = new VerificationCodeGenerator();

    // Check if generator is random enough.
    @Test
    public void testRandomness(){
        /*
        We will generate 10 000 verificationcodes and only allow 0.001% duplicates.
         */
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            strings.add(generator.generate());
        }

        int numberOfDuplicates=0;

        for (String s: strings) {
            int frequency = Collections.frequency(strings, s);
            if(frequency>1){
                numberOfDuplicates++;
            }
        }

        assertTrue(numberOfDuplicates<=1);
    }
}
