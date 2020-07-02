package be.ugent.blok2.model.penalty;

import be.ugent.blok2.helpers.Language;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class TestPenaltyEvent {
    private int code=(int)(Math.random());
    private int points=(int)(Math.random());
    private boolean publicAccessible = true;
    private Map<Language, String> description = new HashMap<>();

    public TestPenaltyEvent(){
        description.put(Language.DUTCH,"Nederlands");
        description.put(Language.ENGLISH,"Engels");
    }

    @Test
    public void testEquals() {
        PenaltyEvent event1 = new PenaltyEvent(code, points, publicAccessible, description);
        PenaltyEvent event2 = new PenaltyEvent(code, points, publicAccessible, description);
        assertEquals(event1,event1);
        assertEquals(event2,event2);
        assertEquals(event1,event2);
    }

    @Test
    public void testHashcode() {
        PenaltyEvent event1 = new PenaltyEvent(code, points, publicAccessible, description);
        PenaltyEvent event2 = new PenaltyEvent(code, points, publicAccessible, description);
        assertEquals(event1.hashCode(),event1.hashCode());
        assertEquals(event2.hashCode(),event2.hashCode());
        assertEquals(event1.hashCode(),event2.hashCode());

    }

    @Test
    public void testClone() {
        PenaltyEvent event1 = new PenaltyEvent(code, points, publicAccessible, description);
        PenaltyEvent event2 = event1.clone();
        assertEquals(event1,event2);
    }
}
