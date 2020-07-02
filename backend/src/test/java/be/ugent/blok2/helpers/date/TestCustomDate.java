package be.ugent.blok2.helpers.date;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TestCustomDate {

    @Test
    public void testStringMethods() {
        assertEquals("1970-01-01T00:00:00", (CustomDate.parseString("1970-01-01T00:00:00")).toString());
        assertEquals(new CustomDate(1970, 1, 1, 0, 0, 0).toString(), (CustomDate.parseString("1970-01-01T00:00:00")).toString());
    }


    @Test
    public void testIsToday() {
        LocalDate localDate = LocalDate.now();
        CustomDate today = new CustomDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        assertTrue(today.isToday());
        CustomDate todayRandomTime = new CustomDate(localDate.getYear(), localDate.getMonthValue(),
                localDate.getDayOfMonth(), (int) (Math.random() * 24), (int) (Math.random() * 60),
                (int) (Math.random() * 60));
        assertTrue(todayRandomTime.isToday());
        CustomDate passed = new CustomDate(2000, 1, 1);
        assertFalse(passed.isToday());
    }

    @Test
    public void testIsSameDay() {
        for (int i = 0; i < 25; i++) {
            int day = (int) (Math.random() * 28);
            int month = (int) (Math.random() * 12);
            int year = 1970 + (int) (Math.random() * 50);
            CustomDate d1 = new CustomDate(year, month, day, (int) (Math.random() * 24), (int) (Math.random() * 60),
                    (int) (Math.random() * 60));
            CustomDate d2 = new CustomDate(year, month, day, (int) (Math.random() * 24), (int) (Math.random() * 60),
                    (int) (Math.random() * 60));
            assertTrue(d1.isSameDay(d2));
            assertTrue(d2.isSameDay(d1));
            assertTrue(d1.isSameDay(d1));
            assertTrue(d2.isSameDay(d2));
        }
    }

    @Test
    public void testEquals() {
        assertEquals(new CustomDate(1970, 1, 1, 0, 0, 0), new CustomDate(1970, 1, 1, 0, 0, 0));
        assertNotEquals(new CustomDate(1970, 1, 1, 0, 0, 0), new CustomDate(5, 2, 0, 5, 8, 9));
    }

    @Test
    public void testHascode() throws Exception {
        assertEquals(new CustomDate(1970, 1, 1, 0, 0, 0).hashCode(), new CustomDate(1970, 1, 1, 0, 0, 0).hashCode());
        assertNotEquals(new CustomDate(1970, 10, 1, 0, 40, 70).hashCode(), new CustomDate(1970, 1, 1, 0, 0, 0).hashCode());
        assertEquals(new CustomDate(1970, 1, 1, 0, 0, 0).hashCode(), CustomDate.parseString("1970-01-01T00:00:00").hashCode());
    }

    @Test
    public void testClone() {
        CustomDate d1 = new CustomDate(1970 + (int) (Math.random() * 50), (int) (Math.random() * 12),
                (int) (Math.random() * 28), (int) (Math.random() * 24), (int) (Math.random() * 60),
                (int) (Math.random() * 60));
        CustomDate d2=d1.clone();
        assertEquals(d1,d2);
    }

}
