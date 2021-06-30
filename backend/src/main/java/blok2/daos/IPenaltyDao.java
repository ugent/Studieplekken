package blok2.daos;

import blok2.model.penalty.Penalty;

import java.util.List;

public interface IPenaltyDao {

    /**
     * Get the actual occurrence of a PenaltyEvent: e.g. someone has cancelled after 17:00 the day before opening
     */
    List<Penalty> getPenaltiesByUser(String userId);

    /**
     * Get all occurrences of a PenaltyEvent within a location
     */
    List<Penalty> getPenaltiesByLocation(int locationId);

    /**
     * Get all occurrences of a PenaltyEvent by type of PenaltyEvent
     */
    List<Penalty> getPenaltiesByEventCode(int eventCode);

    /**
     * addPenalty() adds a Penalty to the so called Penalty Book (like the Order Book in a stock exchange market)
     */
    Penalty addPenalty(Penalty penalty);

    /**
     * deleteEvent() deletes a Penalty
     */
    void deletePenalty(Penalty penalty);
    
}
