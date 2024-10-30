package blok2.http.controllers.authorization;

import blok2.exceptions.NotAuthorizedException;
import blok2.model.users.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.BiFunction;

public abstract class AuthorizedController {
    /**
     * Check if the current user is authorized to perform an action on the given entity.
     * 
     * @param <T> The type of the entity.
     * @param callback The function that checks if the user is authorized for given entity.
     * @param entity The entity to check.
     */
    public <T> void checkAuthorization(BiFunction<T, User, Boolean> callback, T entity) {
        this.checkAuthorization(callback, entity, "You do not have the necessary permissions.");
    }

    /**
     * Check if the user is authorized to perform an action on the given entity.
     * 
     * @param <T> The type of the entity.
     * @param callback The function that checks if the user is authorized.
     * @param entity The entity to check.
     * @param message The message to display if the user is not authorized.
     */
    public <T> void checkAuthorization(BiFunction<T, User, Boolean> callback, T entity, String message) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User) {
            User user = (User) principal;

            if (user.isAdmin()) {
                return;
            }

            if (!callback.apply(entity, user)) {
                throw new NotAuthorizedException(message);
            }
        }
    }

    /**
     * Check if the current user is an admin.
     * 
     * @return True if the current user is an admin, false otherwise.
     */
    public boolean isAdmin() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User) {
            User user = (User) principal;
            return user.isAdmin();
        }
        
        return false;
    }
}
