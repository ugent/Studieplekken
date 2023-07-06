package blok2.database.repositories;

import blok2.model.penalty.PenaltyEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PenaltyEventRepository extends JpaRepository<PenaltyEvent, Integer> {
}
