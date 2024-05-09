package blok2.database.repositories;

import blok2.model.location.LocationTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationTagRepository extends JpaRepository<LocationTag, Integer> {
}
