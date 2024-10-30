package blok2.http.controllers;

import blok2.model.users.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller allows the frontend to get the User-object of the user that
 * is logged in.
 */
@RestController
@RequestMapping("whoAmI")
public class WhoAmIController {

    /**
     * Used by frontend to be able to determine who is logged in, or if
     * the user is anonymous.
     *
     * To avoid errors in the frontend, an empty user is returned.
     * Note that this will not introduce authorization leaks because
     * no GrantedAuthority is given to an empty user, only if User#userId
     * is not empty.
     *
     * @return user object from authenticated user, or an anonymous user
     */
    @GetMapping
    @PreAuthorize("permitAll()")
    public User whoAmI() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        } else {
            return new User();
        }
    }
}
