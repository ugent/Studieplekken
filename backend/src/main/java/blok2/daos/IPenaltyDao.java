package blok2.daos;

import blok2.model.penalty.Penalty;

import java.util.List;

public interface IPenaltyDao {

    /**
     * Get the actual occurrence of a PenaltyEvent: e.g. someone has cancelled after 17:00 the day before opening
     */
    List<Penalty> getPenaltiesByUser(String userId);

    List<Penalty> getAllPenalties();

    /**
     * addPenalty() adds a Penalty
     */
    Penalty addPenalty(Penalty penalty);

    /**
     * deleteEvent() deletes a Penalty
     */
    void deletePenalty(Penalty penalty);

    int getUserPenalty(String userid);
}
