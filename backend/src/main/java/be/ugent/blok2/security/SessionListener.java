package be.ugent.blok2.security;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * This class is used to remove the entry out of the userscache when a
 * session gets destroyed.
 */
@WebListener
public class SessionListener implements HttpSessionListener {

    private static UsersCache usersCache = UsersCache.getInstance();

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // set how long a session can exist when the user is not active
        se.getSession().setMaxInactiveInterval(2*60*60);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        usersCache.sessionDestroyed(se.getSession().getId());
    }
}
