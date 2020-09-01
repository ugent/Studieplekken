package blok2.helpers.generators;

import org.springframework.stereotype.Service;

@Service
public class VerificationCodeGenerator implements IGenerator<String> {
    private static final int VERIFICATION_SIZE = 32;

    /*
     * This method generates a verification code. The size of such
     * a verification code is determined by VerificationCodeGenerator.VERIFICATION_SIZE
     *
     * Every character to be added has a 10% chance of being a number,
     * 40% chance of being a capital letter and 50% chance of being a
     * lower case letter
     */
    public String generate() {
        StringBuilder sb = new StringBuilder();

        double rand;
        char n; // next char
        for (int i = 0; i < VERIFICATION_SIZE; i++) {
            rand = Math.random() * 100;
            if (rand < 10) {
                n = (char) ('0' + (int) (Math.random() * 10));
                sb.append(n);
            } else if (rand < 50) {
                n = (char) ('A' + (int) (Math.random() * 26));
                sb.append(n);
            } else {
                n = (char) ('a' + (int) (Math.random() * 26));
                sb.append(n);
            }
        }

        return sb.toString();
    }
}
