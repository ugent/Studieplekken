package be.ugent.blok2.controllers;

import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.model.users.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

/**
 * This controller handles all requests related to users.
 * Such as registration, list of users, specific users, ...
 */
@RestController
@RequestMapping("api/account")
public class AccountController extends AController {

    private final IAccountDao accountDao;

    public AccountController(IAccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @GetMapping("/{mail}")
    public User getUserByMail(@PathVariable("mail") String mail) {
        try {
            return accountDao.getUserByEmail(mail);
        } catch (SQLException ignore) {
            return null;
        }
    }
}
