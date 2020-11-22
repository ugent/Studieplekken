package blok2.helpers.authorization;

import blok2.model.users.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.AccessControlException;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public abstract class AuthorizedController {



    public <T> void isAuthorized(BiFunction<T, User, Boolean> p, T entity) {
        isAuthorized(p, entity, "You do not have the necessary permissions.");
    }
    public <T> void isAuthorized(BiFunction<T, User, Boolean> p, T entity, String message) {

        Object userO = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(userO instanceof User && ((User)userO).isAdmin()) {
            // Blanket admin permissions. You're authorized.
            return;
        }

        if(!(userO instanceof User) || !p.apply(entity, (User)userO)) {
            throw new AccessControlException(message);
        }
    }

}