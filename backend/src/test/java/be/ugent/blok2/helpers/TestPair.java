package be.ugent.blok2.helpers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TestPair {
    private String first = "first";
    private String second = "second";

    @Test
    public void testGetFirst() {
        Pair<String, String> pair = new Pair<>(first, second);
        assertEquals(first, pair.getFirst());
    }

    @Test
    public void testSetFirst() {
        Pair<String, String> pair = new Pair<>(first, second);
        String newFirst = "new";
        pair.setFirst(newFirst);
        assertEquals(newFirst, pair.getFirst());
    }

    @Test
    public void testGetSecond() {
        Pair<String, String> pair = new Pair<>(first, second);
        assertEquals(second, pair.getSecond());
    }

    @Test
    public void testSetSecond() {
        Pair<String, String> pair = new Pair<>(first, second);
        String newSecond = "new";
        pair.setSecond(newSecond);
        assertEquals(newSecond, pair.getSecond());
    }

    @Test
    public void testEquals() {
        Pair<String, String> pair1 = new Pair<>(first, second);
        Pair<String, String> pair2 = new Pair<>(first, second);
        assertEquals(pair1, pair2);
    }

    @Test
    public void testHascode() {
        Pair<String, String> pair1 = new Pair<>(first, second);
        Pair<String, String> pair2 = new Pair<>(first, second);
        assertEquals(pair1.hashCode(), pair2.hashCode());
    }
}
