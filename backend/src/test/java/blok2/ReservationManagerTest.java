package blok2;

import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import blok2.scheduling.ReservationManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class
})
public class ReservationManagerTest {
    
    @Autowired
    public ReservationManager reservationManager;
    
    // TODO(ydndonck): This test will never end, it's just a basic thing to see if 
    // all reservations arrive at the end of the pipe-line in tact. Should probably make 
    // this into a proper test.
    @Test
    public void test1() {
        Thread reservationProducerThread = new Thread(reservationProducer(5000)); // Pretty much 5000 requests in 1s.
        Thread reservationConsumerThread = new Thread(reservationConsumer());
        Thread randomPoolSchedulerThread = new Thread(randomScheduler());
        Thread poolConsumerThread = new Thread(poolConsumer());
        
        reservationProducerThread.start();
        reservationConsumerThread.start();
        randomPoolSchedulerThread.start();
        poolConsumerThread.start();
        
        try {
            reservationProducerThread.join();
            reservationConsumerThread.join();
            randomPoolSchedulerThread.join();
            poolConsumerThread.join();
        } catch (InterruptedException e) {
            System.err.println("Test interrupted.");
            e.printStackTrace();
        }
    }
    
    
    private Runnable reservationProducer(int amount) {
        // All of these timeslots can be scheduled immediately except for Timeslot 4.
        // Timeslot 4 becomes available for scheduling after about 1 minute. 
        Timeslot[] timeslots = {
                new Timeslot(1, LocalDate.now().plusDays(10), LocalTime.now(), LocalTime.now().plusHours(4), true, LocalDateTime.now(),1000, 0),
                new Timeslot(2, LocalDate.now().plusDays(10), LocalTime.now(), LocalTime.now().plusHours(4), true, LocalDateTime.now().minusMinutes(5), 1000, 0),
                // new Timeslot(),
                new Timeslot(3, LocalDate.now().plusDays(20), LocalTime.now(), LocalTime.now().plusHours(4), true, LocalDateTime.now().minusMinutes(100), 1000, 0),
                new Timeslot(4, LocalDate.now().plusDays(10), LocalTime.now(), LocalTime.now().plusHours(1), true, LocalDateTime.now().plusMinutes(1), 5000, 0)
        };
        User user = new User();
        return () -> {
            System.out.println("[PRODUCER] Creating " + amount + " reservations.");
            long start = System.currentTimeMillis();
            for (int i = 0; i < amount; i += 1) {
                Timeslot timeslot = timeslots[i % timeslots.length];
                reservationManager.addReservationToQueue(new LocationReservation(user, timeslot, LocationReservation.State.APPROVED));
            }
            long end = System.currentTimeMillis();
            System.out.println("[PRODUCER] Done creating " + amount + " reservations (" + (end - start) + "ms).");
            // Second batch after 65 seconds.
            try {
                Thread.sleep(65 * 1000);
            } catch (InterruptedException e) {
                // Do not handle.
            }
            System.out.println("[PRODUCER] Creating " + amount + " reservations.");
            start = System.currentTimeMillis();
            for (int i = 0; i < amount; i += 1) {
                Timeslot timeslot = timeslots[i % timeslots.length];
                reservationManager.addReservationToQueue(new LocationReservation(user, timeslot, LocationReservation.State.APPROVED));
            }
            end = System.currentTimeMillis();
            System.out.println("[PRODUCER] Done creating " + amount + " reservations (" + (end - start) + "ms).");
        };
    }
    
    private Runnable reservationConsumer() {
        return () -> {
            while (true) {
                System.out.println("[R-CONSUMER] Allocating reservations to pools.");
                reservationManager.allocateReservationsToPools();
            }
        };
    }
    
    private Runnable randomScheduler() {
        return () -> {
            while (true) {
                System.out.println("[RANDOM-SCHEDULER] Allocation random pools to pool queue.");
                reservationManager.scheduleRandomPools();
                try {
                    Thread.sleep(20 * 1000);
                } catch (InterruptedException e) {
                    // Do not handle.
                }
            }
        };
    }
    
    private Runnable poolConsumer() {
        return () -> {
            while (true) {
                System.out.println("[P-CONSUMER] Acquiring reservations.");
                List<LocationReservation> reservationList = new ArrayList<>();
                while (reservationList.size() == 0) { 
                  reservationList = reservationManager.getReservationPool();
                }
                System.out.println("[P-CONSUMER] Found " + reservationList.size() + " reservations. Expected process time: " + 15 * reservationList.size() + "ms.");
                try {
                    // 15 ms process time per request (arbitrarily chosen).
                    Thread.sleep(15L * reservationList.size());
                } catch (InterruptedException e) {
                    // Do not handle.
                }
                System.out.println("[P-CONSUMER] Fully consumed " + reservationList.size() + " reservations.");
            }
        };
    }
    
    
    
}
