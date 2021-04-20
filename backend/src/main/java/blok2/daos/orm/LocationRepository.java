package blok2.daos.orm;

import blok2.model.reservables.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Integer> {
    List<String[]> getOpeningHoursOverview(LocalDate monday, LocalDate sunday);
}
