package blok2.daos;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.database.dao.IUserDao;
import blok2.model.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


public class TestDBAccountDao extends BaseTest {

    @Autowired
    private IUserDao userDao;

    private User testUser1;
    private User testUser2;

    @Override
    public void populateDatabase() {
        testUser1 = TestSharedMethods.adminTestUser();
        testUser2 = TestSharedMethods.studentTestUser();

        TestSharedMethods.addTestUsers(userDao, testUser1, testUser2);
    }

    @Override
    public void cleanup() {
        TestSharedMethods.removeTestUsers(userDao, testUser2, testUser1);
    }

    @Test
    public void directlyAddUserTest() {
        User directlyAddedUser = testUser1.clone();
        directlyAddedUser.setUserId("1" + testUser1.getUserId());
        directlyAddedUser.setMail("directly.addeduser@ugent.be");

        userDao.addUser(directlyAddedUser);
        User u = userDao.getUserById(directlyAddedUser.getUserId());
        Assert.assertEquals(directlyAddedUser, u);

        // remove added user
        TestSharedMethods.removeTestUsers(userDao, directlyAddedUser);
    }

    @Test
    public void updateUserTest() {
        User expectedChangedUser = testUser1.clone();

        // change the role opposed to testUser1, update should succeed
        expectedChangedUser.setAdmin(false);

        userDao.updateUser(expectedChangedUser);

        User actualChangedUser = userDao.getUserById(expectedChangedUser.getUserId());
        Assert.assertEquals(expectedChangedUser, actualChangedUser);
    }

    @Test
    public void accountExistsByEmailTest() {
        boolean exists = userDao.accountExistsByEmail(testUser1.getMail());
        Assert.assertTrue(exists);
    }

    @Test
    public void testGetters() {
        User u = userDao.getUserByEmail(testUser1.getMail());
        Assert.assertEquals("getUserByEmail", testUser1, u);

        u = userDao.getUserById(testUser1.getUserId());
        Assert.assertEquals("getUserById", testUser1, u);

        List<User> list = userDao.getUsersByLastName(testUser1.getLastName());
        Assert.assertEquals("getUsersByLastName", 2, list.size());

        list = userDao.getUsersByLastName("last_name_that_has_no_entry");
        Assert.assertEquals("getUsersByLastName" + list.size(), 0, list.size());

        list = userDao.getUsersByFirstName(testUser1.getFirstName());
        Assert.assertEquals("getUsersByFirstName", 1, list.size());

        list = userDao.getUsersByFirstName("first_name_that_has_no_entry");
        Assert.assertEquals("getUsersByFirstName", 0, list.size());

        list = userDao.getUsersByFirstAndLastName(testUser1.getFirstName(), testUser1.getLastName());
        Assert.assertEquals("getUsersByFirstAndLastName", 1, list.size());
    }

    @Test
    public void getUserFromBarcodeTest() {
        // Code 128
        String barcode = testUser1.getUserId();
        User u = userDao.getUserFromBarcode(barcode);
        Assert.assertEquals("getUserFromBarcodeTest, code 128", testUser1, u);

        // For the other codes, add another user
        User user = testUser1.clone();

        String user_student_number = "000140462060";
        String user_upca_barcode = "001404620603";
        String user_ean13_barcode = "0001404620603";
        String user_other_barcode = "0000140462060";

        user.setUserId(user_student_number);
        user.setMail("other_mail_due_to_unique_constraint@ugent.be");

        // Before every following assertion, the user is added, queried with the corresponding
        // encoded student number, and removed. The enclosing additions and removals are
        // included because if assertion fails, the state of the test database wouldn't be reset

        // UPC-A
        userDao.addUser(user);
        u = userDao.getUserFromBarcode(user_upca_barcode);
        userDao.deleteUser(user.getUserId());
        Assert.assertEquals("getUserFromBarcodeTest, UPC-A", user, u);

        // EAN13
        userDao.addUser(user);
        u = userDao.getUserFromBarcode(user_ean13_barcode);
        userDao.deleteUser(user.getUserId());
        Assert.assertEquals("getUserFromBarcodeTest, EAN13", user, u);

        // Other?
        userDao.addUser(user);
        u = userDao.getUserFromBarcode(user_other_barcode);
        userDao.deleteUser(user.getUserId());
        Assert.assertEquals("getUserFromBarcodeTest, Other?", user, u);
    }
}
