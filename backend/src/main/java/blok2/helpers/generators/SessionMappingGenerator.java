package blok2.helpers.generators;

import java.time.Instant;

public class SessionMappingGenerator implements IGenerator<String> {
    private static final int POSTFIX_SIZE = 32;
    private static int cyclicNumber = 0;
    private static final int CYCLE_SIZE = 10000;
    private static final String SYMBOLS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * This method will generate a code that starts with the time in seconds from epoch. A number between 0 and
     * CYCLE_SIZE-1 will be appended to the time and then a random postfix string with a length of POSTFIX_SIZE.
     * This makes sure that this generator will generate at least CYCLE_SIZE unique strings every millisecond.
     */
    @Override
    public String generate() {
        StringBuilder sb = new StringBuilder();
        long l = Instant.now().toEpochMilli();
        sb.append(l);
        int i = (cyclicNumber++ % CYCLE_SIZE);
        sb.append(i);
        for (int j = 0; j < POSTFIX_SIZE; j++) {
            sb.append(SYMBOLS.charAt((int) (Math.random() * SYMBOLS.length())));
        }
        return sb.toString();
    }
}
