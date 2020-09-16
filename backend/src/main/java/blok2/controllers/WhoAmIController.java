package blok2.controllers;

import blok2.model.users.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * This controller allows the frontend to get the User-object of the user that
 * is logged in.
 */
@RestController
@RequestMapping("/whoAmI")
public class WhoAmIController {

    @GetMapping
    public User whoAmI() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not logged in");
        }
    }

}
