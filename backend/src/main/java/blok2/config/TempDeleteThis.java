package blok2.config;

import blok2.daos.repositories.LocationReservationRepository;
import blok2.daos.repositories.TimeslotRepository;
import blok2.daos.repositories.UserRepository;
import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TempDeleteThis {
    
    @Autowired
    private LocationReservationRepository repo;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TimeslotRepository timeslotRepository;
    
    
    @Bean
    public Object doStuffTempDeleteThis() {
        if (repo == null) {
            System.out.println("Repo not wired.");
            return null;
        }
        System.out.println("Repo wired");
        User user = userRepository.findById("000170876109").orElseThrow(() -> new RuntimeException("User not found"));
        Timeslot timeslot = timeslotRepository.getByTimeslotSeqnr(498);
        LocationReservation reservation = new LocationReservation(user, timeslot, null);
         reservation.setState(LocationReservation.State.ABSENT);
        reservation = repo.save(reservation);
        System.out.println(reservation);
        return null;
    }
}
