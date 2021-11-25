package blok2.scheduling;

import blok2.model.reservations.LocationReservation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Implements a thread-safe queue of reservations. Reservations are pushed and popped
 * from the queue. This class is a thin wrapper around LockFreeClearQueue that adds
 * a semaphore to prevent spin-clearing on the clear() method.
 */
public class ReservationQueue {

    private final LockFreeClearQueue<LocationReservation> queue = new LockFreeClearQueue<>();

    // Semaphore indicates whether there are reservations in the queue. It should be non-zero if there are.
    // It does not need to guarantee to be zero if there are no reservations in the queue. These somewhat
    // lax guarantees make a simpler implementation possible while providing the desired
    // effect of blocking instead of needing to spin-clear the queue.
    private final Semaphore semaphore = new Semaphore(0);

    /**
     * Adds a reservation to the reservation-queue.
     * NON-BLOCKING
     */
    public void push(LocationReservation reservation) {
        queue.add(reservation);
        semaphore.release();
    }

    /**
     * Drains the current queue of all reservations. The queue is
     * left empty. The drained reservations are returned. This
     * method does not return until there are reservations to drain.
     * BLOCKING
     */
    public List<LocationReservation> clear() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            return new ArrayList<>();
        }
        // NOTE: Order of the following 2 statements is important. Permits should be drained first.
        semaphore.drainPermits();
        return queue.clear();
    }

}
