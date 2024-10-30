package blok2.database.repository;

import blok2.model.penalty.PenaltyEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PenaltyEventRepository extends JpaRepository<PenaltyEvent, Integer> {
}
