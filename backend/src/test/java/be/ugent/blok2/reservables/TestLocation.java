package be.ugent.blok2.reservables;

import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.helpers.date.Time;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

@SpringBootTest
public class TestLocation {
    @Test
    public void testAddDay() {
        Location location = new Location("test");
        Day day = new Day(new CustomDate(2020,12,5), new Time(10,0,0), new Time(20,0,0), new CustomDate(2019,12,5));
        location.addDay(day);
        Collection<Day> days = location.getCalendar();
        assertTrue(days.contains(day));
    }

    @Test
    public void testRemoveDay() {
        Location location = new Location("test");
        Day day = new Day(new CustomDate(2020,12,5), new Time(10,0,0), new Time(20,0,0), new CustomDate(2019,12,5));
        location.addDay(day);
        Collection<Day> days = location.getCalendar();
        assertTrue(days.contains(day));

        location.removeDay(day);
        assertFalse(location.getCalendar().contains(day));
    }
}
