package blok2.daos.repositories;

import blok2.model.ActionLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionLogRepository extends JpaRepository<ActionLogEntry, Integer> {

}
