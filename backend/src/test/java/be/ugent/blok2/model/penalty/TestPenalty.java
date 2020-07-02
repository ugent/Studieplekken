package be.ugent.blok2.model.penalty;

import be.ugent.blok2.helpers.date.CustomDate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TestPenalty {
    private String augentID = "6463116463";
    private int eventCode = 64534;
    private CustomDate timestamp = new CustomDate();
    private CustomDate reservationDate = new CustomDate();
    private String reservationLocation = "test";
    private int receivedPoints = 3165;

    @Test
    public void testEquals() {
        Penalty p1 = new Penalty(augentID,eventCode, timestamp, reservationDate, reservationLocation,receivedPoints);
        Penalty p2 = new Penalty(augentID,eventCode, timestamp, reservationDate, reservationLocation,receivedPoints);
        assertEquals(p1,p2);
        assertEquals(p1,p1);
        assertEquals(p2,p2);
    }

    @Test
    public void testHashcode() {
        Penalty p1 = new Penalty(augentID,eventCode, timestamp, reservationDate, reservationLocation,receivedPoints);
        Penalty p2 = new Penalty(augentID,eventCode, timestamp, reservationDate, reservationLocation,receivedPoints);
        assertEquals(p1.hashCode(),p2.hashCode());
        assertEquals(p1.hashCode(),p1.hashCode());
        assertEquals(p2.hashCode(),p2.hashCode());
    }

    @Test
    public void testClone() {
        Penalty p1 = new Penalty("0000",(int)(Math.random()),new CustomDate(),new CustomDate(),"",(int)(Math.random()));
        Penalty p2 = p1.clone();
        assertEquals(p1,p2);
    }
}
