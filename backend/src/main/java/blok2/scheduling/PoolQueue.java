package blok2.scheduling;

import blok2.config.PoolProcessor;
import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import static blok2.config.PoolProcessor.RANDOM_RESERVATION_DURATION_MINS;

/**
 * Implements a thread-safe queue of pools. Reservations are added to
 * the queue and automatically sorted into pools. Pools can then be popped
 * from the queue.
 */
public class PoolQueue {

    private final Map<Timeslot, LockFreeClearQueue<LocationReservation>> randomPools = new ConcurrentHashMap<>();
    private final LockFreeClearQueue<LocationReservation> fcfsPool = new LockFreeClearQueue<>();
    private final ConcurrentLinkedQueue<List<LocationReservation>> randomPoolQueue = new ConcurrentLinkedQueue<>();

    // This semaphore indicates the amount of pools currently available.
    private final Semaphore poolSemaphore = new Semaphore(0);

    /**
     * Push a reservation into one of the pools. It is expected that
     * the reservation is ready to be put into the queue.
     * NON-BLOCKING
     */
    public void pushReservation(LocationReservation reservation) {
        Timeslot timeslot = reservation.getTimeslot();
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(timeslot.getReservableFrom())) {
            return; // Ignore, too early.
        }
        // As long as the there still exists a randompool for the timeslot, schedule it in there
        // regardless of the time.
        if (now.isBefore(timeslot.getReservableFrom().plusMinutes(RANDOM_RESERVATION_DURATION_MINS)) || randomPools.containsKey(reservation.getTimeslot())) {
            randomPools.computeIfAbsent(timeslot, timeslotIgnore -> new LockFreeClearQueue<>());
            randomPools.get(timeslot).add(reservation);
            // NOTE: No semaphore release here, because the pre-reservation timer needs to expire first.
            return;
        }
        if (fcfsPool.add(reservation) == 1) {
            poolSemaphore.release();
        }
    }

    /**
     * Moves the pools of random reservations that are ready to be scheduled
     * into the poolqueue. This method is meant to be called somewhat
     * regularly to make sure the random pools are processed in due time.
     * NON-BLOCKING
     */
    public void scheduleRandomPools() {
        LocalDateTime now = LocalDateTime.now();
        Set<Timeslot> timeslots = randomPools.keySet();
        for (Timeslot timeslot : timeslots) {
            if (now.isAfter(timeslot.getReservableFrom().plusMinutes(RANDOM_RESERVATION_DURATION_MINS))) {
                LockFreeClearQueue<LocationReservation> timeslotRandomQueue = randomPools.remove(timeslot);
                if (timeslotRandomQueue == null) {
                    continue;
                }
                List<LocationReservation> reservations = timeslotRandomQueue.clear();
                if (reservations.size() != 0) {
                    Collections.shuffle(reservations);
                    randomPoolQueue.add(reservations);
                    poolSemaphore.release();
                }
            }
        }
    }

    /**
     * Pops a pool of reservations from the pool queue. Doesn't return
     * until there is such a pool available
     * BLOCKING
     */
    public List<LocationReservation> popPool() {
        try {
            poolSemaphore.acquire();
            List<LocationReservation> randomPool = randomPoolQueue.poll();
            if (randomPool == null) {
                List<LocationReservation> reservations = fcfsPool.clear();
                if (reservations.size() == 0) {
                    throw new RuntimeException("Invalid state. No empty pools should be in the queue.");
                }
                return reservations;
            }
            if (randomPool.size() == 0) {
                throw new RuntimeException("Invalid state. No empty pools should be in the queue.");
            }
            return randomPool;
        } catch (InterruptedException ex) {
            return new ArrayList<>();
        }
    }

}
