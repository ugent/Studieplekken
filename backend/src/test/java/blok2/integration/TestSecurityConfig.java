package blok2.integration;

import blok2.TestSharedMethods;
import blok2.database.daos.IUserDao;
import blok2.model.Authority;
import blok2.model.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.sql.SQLException;

@TestConfiguration
public class TestSecurityConfig {
    private static class TestUserDetailsService implements UserDetailsService {
        @Autowired
        private IUserDao userDao;

        @Override
        public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
            try {
                addUsersIfNotExist();
                User user;
                if (s.equals("authholderHoGent")) {
                    user = userDao.getUserByEmail(s + "@hogent.be");
                } else {
                    user = userDao.getUserByEmail(s + "@ugent.be");
                }
                if (user.getUserId().equals("authholder") || user.getUserId().equals("authholderHoGent"))
                    user.getUserAuthorities().add(new Authority()); // kind of hacky but also necessary, its just test code
                return user;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                throw new UsernameNotFoundException(s);
            }
        }

        private void addUsersIfNotExist() throws SQLException {
            try {
                User admin = TestSharedMethods.adminTestUser("admin");
                User student = TestSharedMethods.studentTestUser("student1");
                User student2 = TestSharedMethods.studentTestUser("student2");
                User authorityHolder = TestSharedMethods.studentTestUser("authholder");
                User authorityHolderHoGent = TestSharedMethods.studentTestUserHoGent("authholderHoGent");

                TestSharedMethods.addTestUsers(userDao, admin, student, student2, authorityHolder, authorityHolderHoGent);

            } catch (Exception ignored) {

            }
        }
    }

    @Bean(name = "testUserDetails")
    @Primary
    public UserDetailsService userDetailsService() {
        return new TestUserDetailsService();
    }

}
