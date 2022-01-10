package blok2.config;

import blok2.scheduling.ReservationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReservationProcessor implements Runnable {

    private final ReservationManager reservationManager;

    @Autowired
    public ReservationProcessor(ReservationManager reservationManager) {
        this.reservationManager = reservationManager;
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * Allocate reservations to pools when available. Catch all exceptions
     * because this process must keep running for the continued operation
     * of the application.
     */
    @Override
    public void run() {
        while (true) {
            try {
                reservationManager.allocateReservationsToPools();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
