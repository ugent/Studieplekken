package blok2.http.security.authorization;

import blok2.extensions.exceptions.NotAuthorizedException;
import blok2.model.users.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.BiFunction;

public abstract class AuthorizedController {

    public <T> void isAuthorized(BiFunction<T, User, Boolean> p, T entity) {
        isAuthorized(p, entity, "You do not have the necessary permissions.");
    }

    public <T> void isAuthorized(BiFunction<T, User, Boolean> p, T entity, String message) {
        Object userO = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userO instanceof User && ((User)userO).isAdmin()) {
            // Blanket admin permissions. You're authorized.
            return;
        }

        if (!(userO instanceof User) || !p.apply(entity, (User)userO)) {
            throw new NotAuthorizedException(message);
        }
    }

    public boolean isAdmin() {
        Object userO = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userO instanceof User && ((User)userO).isAdmin();
    }

}
