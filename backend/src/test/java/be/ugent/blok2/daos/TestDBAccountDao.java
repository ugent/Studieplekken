package be.ugent.blok2.daos;

import be.ugent.blok2.TestSharedMethods;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * Note: the test that combines scanner users with locations, is to be found in TestDBScannerLocation.java
 */

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"db", "test"})
public class TestDBAccountDao {

    @Autowired
    private IAccountDao accountDao;

    private User directlyAddedUser;
    private User verifiedAddedUser;

    @Before
    public void setup() {
        TestSharedMethods.setupTestDaoDatabaseCredentials(accountDao);

        directlyAddedUser = TestSharedMethods.employeeAdminTestUser();
        verifiedAddedUser = TestSharedMethods.studentEmployeeTestUser();
    }

    @After
    public void cleanup() {
        accountDao.useDefaultDatabaseConnection();
    }

    @Test
    public void directlyAddUserTest() {
        accountDao.directlyAddUser(directlyAddedUser);
        User u = accountDao.getUserById(directlyAddedUser.getAugentID());
        Assert.assertEquals(directlyAddedUser, u);

        // remove added user
        removeDirectlyAddedUser();
    }

    @Test
    public void addUserToBeVerifiedTest() {
        String verificationCode = accountDao.addUserToBeVerified(verifiedAddedUser);
        accountDao.verifyNewUser(verificationCode);

        User u = accountDao.getUserById(verifiedAddedUser.getAugentID());
        Assert.assertEquals(verifiedAddedUser, u);

        // remove added user
        removeVerifiedAddedUser();
    }

    @Test
    public void updateUserTest() {
        User expectedChangedUser = directlyAddedUser.clone();
        expectedChangedUser.setRoles(new Role[]{Role.STUDENT});

        accountDao.directlyAddUser(directlyAddedUser);
        accountDao.updateUser(expectedChangedUser.getMail(), expectedChangedUser);

        User actualChangedUser = accountDao.getUserById(directlyAddedUser.getAugentID());
        Assert.assertEquals(expectedChangedUser, actualChangedUser);

        // remove added user
        removeDirectlyAddedUser();
    }

    @Test
    public void accountExistsByEmailTest() {
        accountDao.directlyAddUser(directlyAddedUser);
        boolean exists = accountDao.accountExistsByEmail(directlyAddedUser.getMail());
        Assert.assertTrue(exists);

        // remove added user
        removeDirectlyAddedUser();
    }

    @Test
    public void testGetters() {
        accountDao.directlyAddUser(directlyAddedUser);
        accountDao.directlyAddUser(verifiedAddedUser);

        User u = accountDao.getUserByEmail(directlyAddedUser.getMail());
        Assert.assertEquals("getUserByEmail", directlyAddedUser, u);

        u = accountDao.getUserByEmailWithPassword(directlyAddedUser.getMail());
        Assert.assertEquals("getUserByEmailWithPassword", directlyAddedUser, u);

        u = accountDao.getUserById(directlyAddedUser.getAugentID());
        Assert.assertEquals("getUserById", directlyAddedUser, u);

        List<User> list = accountDao.getUsersByLastName(directlyAddedUser.getLastName());
        Assert.assertEquals("getUsersByLastName", 2, list.size());

        list = accountDao.getUsersByLastName("last_name_that_has_no_entry");
        Assert.assertEquals("getUsersByLastName" + list.size(), 0, list.size());

        list = accountDao.getUsersByFirstName(directlyAddedUser.getFirstName());
        Assert.assertEquals("getUsersByFirstName", 1, list.size());

        list = accountDao.getUsersByFirstName("first_name_that_has_no_entry");
        Assert.assertEquals("getUsersByFirstName",0, list.size());

        list = accountDao.getUsersByNameSoundex(directlyAddedUser.getFirstName()
                + " " + directlyAddedUser.getLastName());
        Assert.assertEquals("getUsersByNameSoundex", 1, list.size());

        List<String> names = accountDao.getUserNamesByRole(Role.STUDENT.name());
        Assert.assertEquals("getUserNamesByRole", 2, names.size());

        // remove added users
        removeVerifiedAddedUser();
        removeDirectlyAddedUser();
    }

    private void removeDirectlyAddedUser() {
        accountDao.removeUserById(directlyAddedUser.getAugentID());
        User u = accountDao.getUserById(directlyAddedUser.getAugentID());
        Assert.assertNull(u);
    }

    private void removeVerifiedAddedUser() {
        accountDao.removeUserById(verifiedAddedUser.getAugentID());
        User u = accountDao.getUserById(verifiedAddedUser.getAugentID());
        Assert.assertNull(u);
    }
}
