package be.ugent.blok2.security;

import be.ugent.blok2.helpers.Pair;
import be.ugent.blok2.helpers.generators.IGenerator;
import be.ugent.blok2.helpers.generators.SessionMappingGenerator;
import be.ugent.blok2.helpers.exceptions.NoUserLoggedInWithGivenSessionIdMappingException;
import be.ugent.blok2.helpers.exceptions.UserNotLoggedInException;
import be.ugent.blok2.model.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class is used to store logged in users with their session cookie and mapping cookie.
 * If a user then leaves the application and comes back, he can still be logged in.
 * Implemented as a singleton class.
 */
public class UsersCache {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HashMap<String, Pair<String, User>> users; //Key: generated code, Value: Pair of session id and user
    private final IGenerator<String> sessionMappingGenerator = new SessionMappingGenerator();
    private static final UsersCache instance = new UsersCache();

    public static UsersCache getInstance() {
        return instance;
    }

    private UsersCache() {
        this.users = new HashMap<>();
    }

    /**
     * When a user logs in, he gets logged out from his other sessions and gets a new mapping cookie.
     * His old sessions gets removed out of the storage and his new one gets added.
     */
    public String login(String sessionId, User user) {
        try {
            logout(user);
        } catch (UserNotLoggedInException e) {
        }
        String mappingCode= sessionMappingGenerator.generate();
        users.put(mappingCode, new Pair<>(sessionId, user));
        logger.info("User "+ user.getAugentID() +" logged in with session id "+sessionId);
        return mappingCode;
    }

    /**
     * When a user logs out his mapping cookie gets deleted and removed from the storage.
     */
    public void logout(User user) throws UserNotLoggedInException {
        Iterator<Map.Entry<String, Pair<String, User>>> iterator = this.users.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, Pair<String, User>> next = iterator.next();
            if(next.getValue().getSecond().getAugentID().equals(user.getAugentID())){
                logger.info("Removed user "+user.getAugentID() +" from logged in users");
                iterator.remove();
                return;
            }
        }
        throw new UserNotLoggedInException("User with ID: "+user.getAugentID()+" is not logged in.");
    }

    public User getBySessionIdMapping(String sessionIdMapping) throws NoUserLoggedInWithGivenSessionIdMappingException {
        Pair<String, User> pair = users.get(sessionIdMapping);
        if(pair == null){
            throw new NoUserLoggedInWithGivenSessionIdMappingException("No user is logged in with session id mapping: " + sessionIdMapping);
        }
        User u = pair.getSecond();
        if(u==null)
            throw new NoUserLoggedInWithGivenSessionIdMappingException("No user is logged in with session id mapping: " + sessionIdMapping);
        return u;
    }

    /**
     * Check if session id and mapping cookie match and are in the current storage.
     */
    public boolean isValid(String sessionId, String mapping){
        try {
            return this.users.get(mapping).getFirst().equals(sessionId);
        }
        catch (Exception e){
            return false;
        }
    }

    /**
     * When a session gets destroyed by the back-end then this method wil remove
     * the corresponding entry in the storage.
     */
    public void sessionDestroyed(String sessionId){
        Iterator<Map.Entry<String, Pair<String, User>>> iterator = this.users.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, Pair<String, User>> next = iterator.next();
            if(next.getValue().getFirst().equals(sessionId)){
                iterator.remove();
                return;
            }
        }
    }
}
