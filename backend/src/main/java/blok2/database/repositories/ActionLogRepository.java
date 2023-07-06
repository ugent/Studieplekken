package blok2.database.repositories;

import blok2.model.ActionLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionLogRepository extends JpaRepository<ActionLogEntry, Integer> {

}
