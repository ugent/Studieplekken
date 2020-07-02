package be.ugent.blok2.daos.dummies;

import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.daos.IPenaltyEventsDao;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.Resources;
import be.ugent.blok2.helpers.Variables;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.exceptions.AlreadyExistsException;
import be.ugent.blok2.helpers.exceptions.NoSuchPenaltyEventException;
import be.ugent.blok2.helpers.exceptions.NoSuchUserException;
import be.ugent.blok2.model.penalty.Penalty;
import be.ugent.blok2.model.penalty.PenaltyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Profile("dummy")
public class DummyPenaltyEventsDao implements IPenaltyEventsDao {
    private Map<Integer, PenaltyEvent> events;
    private Map<String, List<Penalty>> penalty_book; // get Penalties for a user
    public static PenaltyEvent TESTPENALTY;

    public DummyPenaltyEventsDao() {
        events = new HashMap<>();
        penalty_book = new HashMap<>();

        Map<Language, String> code0 = new HashMap<>();
        Map<Language, String> code1 = new HashMap<>();
        Map<Language, String> code2 = new HashMap<>();

        TESTPENALTY = new PenaltyEvent(16660, 30, true, code0);

        events.put(16660, TESTPENALTY); // cancelling too late
        events.put(16661, new PenaltyEvent(16661, 50, true, code1)); // coming too late
        events.put(16662, new PenaltyEvent(16662, Variables.thresholdPenaltyPoints, true, code2));

        code0.put(Language.ENGLISH, "Cancelling too late.");
        code0.put(Language.DUTCH, "Te laat annuleren.");

        code1.put(Language.ENGLISH, "Not showing up at all.");
        code1.put(Language.DUTCH, "Helemaal niet komen opdagen.");

        code2.put(Language.ENGLISH, "Blacklist Event.");
        code2.put(Language.DUTCH, "Blacklist Event.");
    }

    @Override
    public List<PenaltyEvent> getPenaltyEvents() {
        List<PenaltyEvent> ret = new ArrayList<>();
        for (int c : events.keySet())
            ret.add(getPenaltyEvent(c));
        return ret;
    }

    @Override
    public PenaltyEvent getPenaltyEvent(int code) throws NoSuchPenaltyEventException {
        if (events.containsKey(code)) {
            return events.get(code).clone();
        } else {
            throw new NoSuchPenaltyEventException("Penalty event with code " + code + " doesn't exist.");
        }
    }

    @Override
    public List<Penalty> getPenalties(String augentId) throws NoSuchUserException {
        if (penalty_book.containsKey(augentId)) {
            List<Penalty> p = new ArrayList<>();
            for (Penalty _p : penalty_book.get(augentId)) {
                p.add(_p.clone());
            }
            return p;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void addPenaltyEvent(PenaltyEvent event) throws AlreadyExistsException {
        int code = event.getCode();
        if (!events.containsKey(code)) {
            events.put(code, event);
        } else {
            throw new AlreadyExistsException("A penalty event with code " + code + " already exists. " +
                    "Use updatePenaltyEvent() instead.");
        }
    }

    @Override
    public void addDescription(int code, Language language, String description) throws AlreadyExistsException, NoSuchPenaltyEventException {
        if (events.containsKey(code)) {
            Map<Language, String> d = events.get(code).getDescriptions();

            if (d.containsKey(language))
                throw new AlreadyExistsException("Description for event " + code + " in " + language
                        + " already exists. Use updateDescription() instead.");

            d.put(language, description);
        } else {
            throw new NoSuchPenaltyEventException("No PenaltyEvent with code " + code + " exists. ");
        }
    }

    @Override
    public void addPenalty(Penalty penalty) throws NoSuchUserException, NoSuchPenaltyEventException {
        if (penalty_book.containsKey(penalty.getAugentID()))
            penalty_book.get(penalty.getAugentID()).add(penalty);
        else {
            List<Penalty> l = new ArrayList<>();
            l.add(penalty);
            penalty_book.put(penalty.getAugentID(), l);
        }
    }

    @Override
    public void updatePenaltyEvent(int code, PenaltyEvent event) throws NoSuchPenaltyEventException {
        if (events.containsKey(code)) {
            // replace the current PenaltyEvent corresponding to code
            if (!event.equals(events.get(code)))
                events.put(code, event);
        } else {
            throw new NoSuchPenaltyEventException("Updating PenaltyEvent: No PenaltyEvent with code " + code
                    + " exists. Use addPenaltyEvent() instead.");
        }
    }

    @Override
    public void updatePenalties(String augentID, List<Penalty> remove, List<Penalty> add) throws NoSuchUserException, NoSuchPenaltyEventException {
        List<Penalty> p;
        if (penalty_book.containsKey(augentID)) {
            p = penalty_book.get(augentID);
        } else {
            p = new ArrayList<>();
            penalty_book.put(augentID, p);
        }

        /*
        for (Penalty _p : remove) System.out.println(_p);
        System.out.println("-------------------");
        for (Penalty _p : add) System.out.println(_p);
        */
        // new penalties should have the correct points filled in
        for(Penalty _p: add){
            if(_p.getReceivedPoints() < 0 ){
                PenaltyEvent e = getPenaltyEvent(_p.getEventCode());
                _p.setReceivedPoints(e.getPoints());
            }
        }

        p.removeAll(remove);
        p.addAll(add);
    }

    @Override
    public void deletePenaltyEvent(int code) throws NoSuchPenaltyEventException {
        if (events.containsKey(code)) {
            events.remove(code);
        } else {
            throw new NoSuchPenaltyEventException("No PenaltyEvent with code " + code + " exists. ");
        }
    }

    @Override
    public void deleteDescription(int code, Language language) throws NoSuchPenaltyEventException {
        if (events.containsKey(code)) {
            PenaltyEvent event = events.get(code);
            if (event.getDescriptions().containsKey(language)) {
                event.getDescriptions().remove(language);
            } else {
                throw new NoSuchPenaltyEventException("PenaltyEvent with code " + code + " has been found, but " +
                        "it has no description in " + language + ".");
            }
        }  else {
            throw new NoSuchPenaltyEventException("No PenaltyEvent with code " + code + " exists. ");
        }
    }

    @Override
    public void deletePenalty(Penalty penalty) throws NoSuchUserException, NoSuchPenaltyEventException {
        if (penalty_book.containsKey(penalty.getAugentID())) {
            List<Penalty> l = penalty_book.get(penalty.getAugentID());
            l.remove(penalty);
        }
    }
}
