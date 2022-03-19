package blok2.scheduling;

import blok2.daos.repositories.LocationReservationRepository;
import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReservationManager {

    private final PoolQueue poolQueue = new PoolQueue();
    private final ReservationQueue reservationQueue = new ReservationQueue();

    private final LocationReservationRepository reservationRepository;

    public ReservationManager(LocationReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
        this.scheduleOnInitialize();
    }

    /**
     * Adds a reservation to the reservation-queue.
     * NON-BLOCKING
     */
    public void addReservationToQueue(LocationReservation reservation) {
        reservationQueue.push(reservation);
    }

    /**
     * Moves the reservations from the reservation-queue into the
     * pool-queue. This is method does not return until there are
     * reservations in the reservation-queue.
     * BLOCKING
     */
    public void allocateReservationsToPools() {
        List<LocationReservation> reservations = reservationQueue.clear();
        LocalDateTime now = LocalDateTime.now();
        for (LocationReservation reservation : reservations) {
            Timeslot timeslot = reservation.getTimeslot();
            LocalDateTime randomReservationStart = timeslot.getReservableFrom();
            if (now.isBefore(randomReservationStart)) {
                // Ignore reservation, because it is too early.
                Optional<LocationReservation> optDbRes = reservationRepository.findById(reservation.getId());
                if (!optDbRes.isPresent()) {
                    continue;
                }
                LocationReservation dbRes = optDbRes.get();
                System.out.println("WARNING: location rejected because of too early");
                dbRes.setState(LocationReservation.State.REJECTED);
                reservationRepository.save(dbRes);
                continue;
            }
            poolQueue.pushReservation(reservation);
        }
    }

    /**
     * Moves the the random-pools into the pool-queue if the are ready to be scheduled.
     * This method is meant to be called somewhat regularly, to make sure the random-pools are processed
     * in due time.
     * NON-BLOCKING
     */
    public void scheduleRandomPools() {
        poolQueue.scheduleRandomPools();
    }

    /**
     * Pops a pool of reservations from the poolqueue. Does not
     * return until such a pool is available.
     * BLOCKING
     * @return : A list of reservations that is ready to be processed.
     */
    public List<LocationReservation> getPool() {
        return poolQueue.popPool();
    }

    private void scheduleOnInitialize() {
        reservationRepository.findAllPending().forEach(this::addReservationToQueue);
    }

}
