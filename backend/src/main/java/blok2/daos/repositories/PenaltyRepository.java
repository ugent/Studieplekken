package blok2.daos.repositories;

import blok2.model.penalty.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PenaltyRepository extends JpaRepository<Penalty, Integer> {

    @Query("select p from Penalty p where p.user_id = ?1")
    List<Penalty> findAllByDesignee(String userId);

    @Query("select p from Penalty p where p.user_id = ?1 and p.timeslotSequenceNumber = ?2")
    List<Penalty> findAllByLocationReservation(String userid, int timeslot_sequence_number);

}
