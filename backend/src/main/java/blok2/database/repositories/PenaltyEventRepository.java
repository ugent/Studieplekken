package blok2.daos.repositories;

import blok2.model.penalty.PenaltyEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PenaltyEventRepository extends JpaRepository<PenaltyEvent, Integer> {
}
