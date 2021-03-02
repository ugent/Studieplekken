package blok2.integration;

import blok2.TestSharedMethods;
import blok2.daos.IAccountDao;
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
        private IAccountDao accountDao;

        @Override
        public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
            System.out.println("called the loadusername");
            try {
                addUsersIfNotExist();
                System.out.println(accountDao.getUserByEmail(s + "@ugent.be"));
                return accountDao.getUserByEmail(s + "@ugent.be");
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
                TestSharedMethods.addTestUsers(accountDao, admin, student, student2);
            } catch (Exception ignored) {

            }
        }
    }

    @Bean(name="testUserDetails")
    @Primary
    public UserDetailsService userDetailsService() {
        return new TestUserDetailsService();
    }

}
