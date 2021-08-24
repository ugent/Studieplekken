package blok2.security.services;

import blok2.daos.IUserDao;
import blok2.model.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class BackdoorUserDetailService {

    private final IUserDao userDao;

    @Autowired
    public BackdoorUserDetailService(IUserDao userDao) {
        this.userDao = userDao;
    }

    public User loadUserDetails(String email) throws UsernameNotFoundException {

        // Try to find the user with given id in the application database
        return this.userDao.getUserByEmail(email);
    }
}
