package blok2.daos.repositories;

import blok2.model.penalty.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PenaltyRepository extends JpaRepository<Penalty, Penalty.PenaltyId> {

    @Query("select p from Penalty p where p.penaltyId.userId = ?1")
    List<Penalty> findAllByUserId(String userId);

    @Query("select p from Penalty p where p.reservationLocation.locationId = ?1")
    List<Penalty> findAllByLocationId(int locationId);

    @Query("select p from Penalty p where p.penaltyId.eventCode = ?1")
    List<Penalty> findAllByPenaltyEventCode(int eventCode);

}
