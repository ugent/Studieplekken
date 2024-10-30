package blok2.database.repository;

import blok2.model.ActionLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionLogRepository extends JpaRepository<ActionLogEntry, Integer> {

}
