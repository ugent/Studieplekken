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

    private User testUser1;
    private User testUser2;

    @Before
    public void setup() {
        TestSharedMethods.setupTestDaoDatabaseCredentials(accountDao);

        testUser1 = TestSharedMethods.employeeAdminTestUser();
        testUser2 = TestSharedMethods.studentEmployeeTestUser();

        TestSharedMethods.addTestUsers(accountDao, testUser1, testUser2);
    }

    @After
    public void cleanup() {
        TestSharedMethods.removeTestUsers(accountDao, testUser2, testUser1);
        accountDao.useDefaultDatabaseConnection();
    }

    @Test
    public void directlyAddUserTest() {
        User directlyAddedUser = testUser1.clone();
        directlyAddedUser.setAugentID("1" + testUser1.getAugentID());
        directlyAddedUser.setMail("directly.addeduser@ugent.be");

        accountDao.directlyAddUser(directlyAddedUser);
        User u = accountDao.getUserById(directlyAddedUser.getAugentID());
        Assert.assertEquals(directlyAddedUser, u);

        // remove added user
        TestSharedMethods.removeTestUsers(accountDao, directlyAddedUser);
    }

    @Test
    public void addUserToBeVerifiedTest() {
        User verifiedAddedUser = testUser1.clone();
        verifiedAddedUser.setAugentID("1" + testUser1.getAugentID());
        verifiedAddedUser.setMail("Verified.AddedUser@UGent.be");

        String verificationCode = accountDao.addUserToBeVerified(verifiedAddedUser);
        accountDao.verifyNewUser(verificationCode);

        User u = accountDao.getUserById(verifiedAddedUser.getAugentID());
        Assert.assertEquals(verifiedAddedUser, u);

        // remove added user
        TestSharedMethods.removeTestUsers(accountDao, verifiedAddedUser);
    }

    @Test
    public void updateUserTest() {
        User expectedChangedUser = testUser1.clone();
        expectedChangedUser.setRoles(new Role[]{Role.STUDENT});

        accountDao.updateUser(testUser1.getMail(), expectedChangedUser);

        User actualChangedUser = accountDao.getUserById(expectedChangedUser.getAugentID());
        Assert.assertEquals(expectedChangedUser, actualChangedUser);
    }

    @Test
    public void accountExistsByEmailTest() {
        boolean exists = accountDao.accountExistsByEmail(testUser1.getMail());
        Assert.assertTrue(exists);
    }

    @Test
    public void testGetters() {
        User u = accountDao.getUserByEmail(testUser1.getMail());
        Assert.assertEquals("getUserByEmail", testUser1, u);

        u = accountDao.getUserByEmailWithPassword(testUser1.getMail());
        Assert.assertEquals("getUserByEmailWithPassword", testUser1, u);

        u = accountDao.getUserById(testUser1.getAugentID());
        Assert.assertEquals("getUserById", testUser1, u);

        List<User> list = accountDao.getUsersByLastName(testUser1.getLastName());
        Assert.assertEquals("getUsersByLastName", 2, list.size());

        list = accountDao.getUsersByLastName("last_name_that_has_no_entry");
        Assert.assertEquals("getUsersByLastName" + list.size(), 0, list.size());

        list = accountDao.getUsersByFirstName(testUser1.getFirstName());
        Assert.assertEquals("getUsersByFirstName", 1, list.size());

        list = accountDao.getUsersByFirstName("first_name_that_has_no_entry");
        Assert.assertEquals("getUsersByFirstName",0, list.size());

        list = accountDao.getUsersByNameSoundex(testUser1.getFirstName()
                + " " + testUser1.getLastName());
        Assert.assertEquals("getUsersByNameSoundex", 1, list.size());

        List<String> names = accountDao.getUserNamesByRole(Role.STUDENT.name());
        Assert.assertEquals("getUserNamesByRole", 2, names.size());
    }

    @Test
    public void getUserFromBarcodeTest() {
        // Code 128
        String barcode = testUser1.getAugentID();
        User u = accountDao.getUserFromBarcode(barcode);
        Assert.assertEquals("getUserFromBarcodeTest, code 128", testUser1, u);

        // For the other codes, add another user
        User user = testUser1.clone();

        String user_student_number = "000140462060";
        String user_upca_barcode = "001404620603";
        String user_ean13_barcode = "0001404620603";
        String user_other_barcode = "0000140462060";

        user.setAugentID(user_student_number);
        user.setMail("other_mail_due_to_unique_constraint@ugent.be");

        // Before every following assertion, the user is added, queried with the corresponding
        // encoded student number, and removed. The enclosing additions and removals are
        // included because if assertion fails, the state of the test database wouldn't be reset

        // UPC-A
        accountDao.directlyAddUser(user);
        u = accountDao.getUserFromBarcode(user_upca_barcode);
        accountDao.removeUserById(user.getAugentID());
        Assert.assertEquals("getUserFromBarcodeTest, UPC-A", user, u);

        // EAN13
        accountDao.directlyAddUser(user);
        u = accountDao.getUserFromBarcode(user_ean13_barcode);
        accountDao.removeUserById(user.getAugentID());
        Assert.assertEquals("getUserFromBarcodeTest, EAN13", user, u);

        // Other?
        accountDao.directlyAddUser(user);
        u = accountDao.getUserFromBarcode(user_other_barcode);
        accountDao.removeUserById(user.getAugentID());
        Assert.assertEquals("getUserFromBarcodeTest, Other?", user, u);
    }
}
