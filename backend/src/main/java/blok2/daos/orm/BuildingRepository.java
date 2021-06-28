package blok2.daos.orm;

import blok2.model.Building;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepository extends JpaRepository<Building, Integer> {
}
