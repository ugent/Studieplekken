package blok2.scheduling;

import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Service
public class ReservationManager {
    
    private static final int RANDOM_RESERVATION_DURATION_MINS = 10;
    
    private final LockFreeClearQueue<LocationReservation> reservationQueue = new LockFreeClearQueue<>(); // thread-safe queue
    // NOTE(ydndonck): This semaphore is used to prevent the thread that tries to acquire it
    // from being scheduled at all when there are no reservations to acquire. This Semaphore should
    // be zero when there are no reservations. However, for ease and speed of the implementation, it only guarantees
    // that it is non-zero when there are reservations.
    private final Semaphore reservationSemaphore = new Semaphore(0);
    
    private final Map<Timeslot, LockFreeClearQueue<LocationReservation>> randomPoolMap = new ConcurrentHashMap<>(); // Thread-safe map
    private final LockFreeClearQueue<LocationReservation> fcfsPool = new LockFreeClearQueue<>(); // thread-safe queue
    private final LockFreeGetQueue<List<LocationReservation>> randomPoolQueue = new LockFreeGetQueue<>(); // thread-safe queue
    // NOTE(ydndonck): This semaphore is used to prevent the thread that tries to acquire it
    // from being scheduled at all when there are no reservations to acquire. This Semaphore should
    // be zero when there are no reservations. However, for ease and speed of the implementation, it only guarantees
    // that it is non-zero when there are reservations.
    private final Semaphore poolSemaphore = new Semaphore(0);
    // TODO(ydndonck): This currently releases once for every RESERVATION instead of every POOL. This
    // is not efficient and means most pool acquisitions result in acquiring an empty pool.
    // Infrastructure to support this is already in place.
    

    /**
     * Adds a reservation to the reservation-queue in the ReservationManager.
     * This method should be fast and non-blocking.
     */
    public void addReservationToQueue(LocationReservation reservation) {
        reservationQueue.add(reservation);
        reservationSemaphore.release(); // TODO(ydndonck): Assumed that this is non-blocking. This is true for release() right?
    }

    /**
     * Removes reservations from the reservation-queue and adds them to the pools.
     */
    public void allocateReservationsToPools() {
        // NOTE(ydndonck): drainPermits() sets the semaphore back to 0. It is possible that it is incremented before the
        // following queue.clear() method, but that is fine. It means that this thread will possibly
        // acquire an empty queue ONCE after that. This is acceptable behaviour considering this Semaphore
        // is only meant as a crude way to prevent this thread from being scheduled when the queue is 
        // empty long-term.
        try {
            reservationSemaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }
        reservationSemaphore.drainPermits();
        List<LocationReservation> reservations = reservationQueue.clear();
        LocalDateTime now = LocalDateTime.now();
        for (LocationReservation reservation : reservations) {
            Timeslot timeslot = reservation.getTimeslot();
            LocalDateTime randomReservationStart = timeslot.getReservableFrom().minusMinutes(RANDOM_RESERVATION_DURATION_MINS);
            LocalDateTime fcfsReservationStart = timeslot.getReservableFrom();
            if (now.isBefore(randomReservationStart)) {
                continue; // Ignore reservation, because too early.
            }
            // As long as the there still exists a randompool for the timeslot, schedule it in there
            // regardless of the time.
            if (now.isBefore(fcfsReservationStart) || randomPoolMap.containsKey(timeslot)) {
                // schedule for random reservation
                randomPoolMap.computeIfAbsent(timeslot, timeslotIgnore -> new LockFreeClearQueue<>());
                randomPoolMap.get(timeslot).add(reservation);
                continue;
            }
            fcfsPool.add(reservation);
            poolSemaphore.release();
        }
    }

    /**
     * Moves the the random-pools into the pool-queue if the are ready to be scheduled.
     * This method is meant to be called somewhat regularly, to make sure the random-pools are processed
     * in due time.
     */
    public void scheduleRandomPools() {
        LocalDateTime now = LocalDateTime.now();
        Set<Timeslot> timeslotSet = randomPoolMap.keySet();
        for (Timeslot timeslot : timeslotSet) {
            if (now.isAfter(timeslot.getReservableFrom())) { 
                LockFreeClearQueue<LocationReservation> queue1 = randomPoolMap.get(timeslot);
                randomPoolMap.remove(timeslot);
                List<LocationReservation> reservations = queue1.clear();
                Collections.shuffle(reservations);
                randomPoolQueue.add(reservations);
                poolSemaphore.release();
            }
        }
    }
    
    public List<LocationReservation> getReservationPool() {
        try {
            poolSemaphore.acquire();
            List<LocationReservation> randomPool = randomPoolQueue.get();
            if (randomPool != null && randomPool.size() != 0) {
                return randomPool;
            }
            return fcfsPool.clear();
        } catch (InterruptedException e) {
            return new ArrayList<>();
        }
    }
    
}
