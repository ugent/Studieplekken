package be.ugent.blok2.model.users;

import be.ugent.blok2.helpers.date.CustomDate;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 * @author bram_
 */
@SpringBootTest
public class TestUser {
    private static final User TEST_STUDENT = new User("1707633", "Van de Walle", "Bram"
            , "bram.vandewalle@ugent.be", "testpwd", "UGent"
            , new Role[]{Role.STUDENT},"");
    
    // Testing if two users with same augentID are the same
    @Test
    public void testEquals1() {
        User u1 = TEST_STUDENT;

        User u2 = new User();
        u2.setAugentID(TEST_STUDENT.getAugentID());

        assertEquals(u1,u2);
        assertEquals(u1,u1);
        assertEquals(u2,u2);
    }
    
    /*
    * If the polymorphic classes of two users are not the same, the
    * equals method should return false
    *
    * A user cannot be equal to null
     */
    @Test
    public void testEquals2() {
        User u1 = TEST_STUDENT;
        User u2 = new User();
        assertEquals(false, u1.equals(u2), "A Student cannot be equal to an Employee");
        
        u2 = null;
        assertEquals(false, u1.equals(u2), "A User cannot be equal to null");
    }

    /*
    * Check if clone creates an exact copy.
     */
    @Test
    public void testClone(){
        User clone = TEST_STUDENT.clone();
        assertEquals(clone, TEST_STUDENT);
    }

    /*
     * Check if cloneToSendableUser creates an exact copy without the password.
     * The password field has to be an empty string.
     */
    @Test
    public void testCloneToSendableUser(){
        User clone = TEST_STUDENT.cloneToSendableUser();
        assertEquals(clone.getPassword(), "");
        assertEquals(TEST_STUDENT.getAugentID(),clone.getAugentID());
        assertEquals(TEST_STUDENT.getBarcode(),clone.getBarcode());
        assertEquals(TEST_STUDENT.getAuthorities(),clone.getAuthorities());
        assertEquals(TEST_STUDENT.getFirstName(),clone.getFirstName());
        assertEquals(TEST_STUDENT.getInstitution(),clone.getInstitution());
        assertEquals(TEST_STUDENT.getLastName(),clone.getLastName());
        assertEquals(TEST_STUDENT.getMail(),clone.getMail());
        assertEquals(TEST_STUDENT.getPenaltyPoints(),clone.getPenaltyPoints());
    }

}
