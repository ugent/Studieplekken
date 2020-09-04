package blok2.daos;

import blok2.model.Authority;
import blok2.model.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

public class TestDBAuthorityDaoWithUser extends TestDao {

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private IAccountDao accountDao;

    private Authority testAuthority;
    private Authority testAuthority2;
    private User testUser;

    @Override
    public void populateDatabase() throws SQLException {
        testAuthority = TestSharedMethods.insertTestAuthority(authorityDao);
        testAuthority2 = TestSharedMethods.insertTestAuthority2(authorityDao);
        testUser = TestSharedMethods.employeeAdminTestUser();
        accountDao.directlyAddUser(testUser);
        authorityDao.addUserToAuthority(testUser.getAugentID(), testAuthority.getAuthorityId());
    }

    @Test
    public void getAuthoritiesFromUser() throws SQLException {
        List<Authority> authorities = authorityDao.getAuthoritiesFromUser(testUser.getAugentID());
        Assert.assertEquals(1, authorities.size());
        Assert.assertTrue(authorities.contains(testAuthority));
    }

    @Test
    public void getUsersFromAuthority() throws SQLException {
        List<User> users = authorityDao.getUsersFromAuthority(testAuthority.getAuthorityId());
        Assert.assertEquals(1, users.size());
        Assert.assertTrue(users.contains(testUser));
        users = authorityDao.getUsersFromAuthority(testAuthority2.getAuthorityId());
        Assert.assertTrue(users.isEmpty());
    }

    @Test
    public void addAndRemoveUserFromAuthority() throws SQLException {
        //todo redo testUsers in TestSharedMethods so they are inserted directly
        User user = TestSharedMethods.employeeAdminTestUser();
        user.setAugentID("000010");
        user.setMail("newtestMail@ugent.be");
        accountDao.directlyAddUser(user);
        authorityDao.addUserToAuthority(user.getAugentID(), testAuthority.getAuthorityId());
        List<User> users = authorityDao.getUsersFromAuthority(testAuthority.getAuthorityId());
        Assert.assertEquals(2, users.size());
        Assert.assertTrue(users.contains(user));
        users = authorityDao.getUsersFromAuthority(testAuthority2.getAuthorityId());
        Assert.assertTrue(users.isEmpty());
        authorityDao.removeUserFromAuthority(user.getAugentID(), testAuthority.getAuthorityId());
        users = authorityDao.getUsersFromAuthority(testAuthority.getAuthorityId());
        Assert.assertEquals(1, users.size());
        Assert.assertFalse(users.contains(user));
        users = authorityDao.getUsersFromAuthority(testAuthority2.getAuthorityId());
        Assert.assertTrue(users.isEmpty());
    }
}
