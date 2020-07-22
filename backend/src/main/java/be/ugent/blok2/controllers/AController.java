package be.ugent.blok2.controllers;

import be.ugent.blok2.helpers.exceptions.NoUserLoggedInWithGivenSessionIdMappingException;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.security.UsersCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * Abstract class for the controllers with basic functionality.
 */
public abstract class AController {
    @Autowired
    Environment env;

    public boolean isProfileActive(String profile){
        return Arrays.asList(env.getActiveProfiles()).contains(profile);
    }

    public boolean isTesting(){
        return isProfileActive("test");
    }

    public User getCurrentUser(HttpServletRequest request) throws NoUserLoggedInWithGivenSessionIdMappingException {
        if (!isTesting()) {
            User u=null;
            // get user from request with its cookie
            Cookie[] cookies = request.getCookies();
            for (int i = 0; i <cookies.length ; i++) {
                if(cookies[i].getName().equals("mapping")){
                    u= UsersCache.getInstance().getBySessionIdMapping(cookies[i].getValue());
                }
            }
            return u;
        }
        return null;
    }
}
