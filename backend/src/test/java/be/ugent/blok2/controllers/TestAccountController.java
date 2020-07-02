package be.ugent.blok2.controllers;

import be.ugent.blok2.configuration.RestAPITestAdapter;
import be.ugent.blok2.configuration.SecurityConfig;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SecurityConfig.class)
@ActiveProfiles({"dummy","test"})
@AutoConfigureMockMvc
public class TestAccountController {
    private static final String BASE_URL = "/api/account";
    private RestAPITestAdapter restAPITestAdapter;

    /*
     * In case you want to receive the verification mail replace this constant with
     * your mail address. Note: make sure your email domain is one of the allowed
     * domains defined in the LDAP dao.
     */
    private String NONEXISTING_MAIL = "gibberish.gibberish@ugent.be";
    private String NONEXISTING_ID = "1999122700000";
    private String NONEXISTING_FIRSTNAME ="first";
    private String NONEXISTING_LASTNAME ="last";

    private User TEST_USER = new User("000170058073", "Doe", "John"
            , "paulien.callebaut@ugent.be", "john", "UGent",new Role[]{Role.STUDENT},25,"001700580731");

    @Autowired
    public TestAccountController(MockMvc mockMvc) {
        restAPITestAdapter = new RestAPITestAdapter(mockMvc);
    }

    @BeforeEach
    public void addTestUser() throws Exception {
        restAPITestAdapter.postCreated(BASE_URL+ "/new/by/employee", TEST_USER);
    }

    @AfterEach
    public void removeTestUser() throws Exception {
        restAPITestAdapter.deleteNoContent(BASE_URL+ '/' + TEST_USER.getAugentID());
    }

    @Test
    public void testGetUserByEmail() throws Exception {
        User user = restAPITestAdapter.getOk(BASE_URL + "/email/" + TEST_USER.getMail(), User.class);
    }

    @Test
    public void testGetUserByEmailNonExisting() throws Exception {
        User user = restAPITestAdapter.getNonExisting(BASE_URL + "/email/" + NONEXISTING_MAIL, User.class);
    }

    @Test
    public void testGetUserById() throws Exception {
        User user = restAPITestAdapter.getOk(BASE_URL + '/' + TEST_USER.getAugentID(), User.class);
    }

    @Test
    public void testGetUserByIdNonExisting() throws Exception {
        User user = restAPITestAdapter.getNonExisting(BASE_URL + '/' + NONEXISTING_ID, User.class);
        user = restAPITestAdapter.getNonExisting(BASE_URL + '/' + "-----------", User.class);
    }

    @Test
    public void testGetUsersByLastname() throws Exception {
        User[] users = restAPITestAdapter.getOk(BASE_URL + "/lastName/" + TEST_USER.getLastName(), User[].class);
    }

    @Test
    public void testGetUsersByLastnameNonExisting() throws Exception {
        User[] users = restAPITestAdapter.getOk(BASE_URL + "/lastName/" + NONEXISTING_LASTNAME, User[].class);
    }

    @Test
    public void testGetUsersByFirstname() throws Exception {
        User[] users = restAPITestAdapter.getOk(BASE_URL + "/firstName/" + TEST_USER.getFirstName(), User[].class);
    }

    @Test
    public void testGetUsersByFirstnameNonExisting() throws Exception {
        User[] users = restAPITestAdapter.getOk(BASE_URL + "/firstName/" + NONEXISTING_FIRSTNAME, User[].class);
    }

    @Test
    public void testUpdateUser() throws Exception {
        User user = restAPITestAdapter.getOk(BASE_URL + "/" + TEST_USER.getAugentID(), User.class);
        String newBarcode = "00000000000";
        user.setBarcode(newBarcode);
        restAPITestAdapter.put(BASE_URL+"/"+user.getMail(),user);
        user = restAPITestAdapter.getOk(BASE_URL + "/" + TEST_USER.getAugentID(), User.class);
        assertEquals(user.getBarcode(), newBarcode);
    }

    @Test
    public void testUpdateNonExistingUser() throws Exception {
        User user = new User("00000000","last","first","mail@ugent.be","test",
                "UGENT",new Role[]{Role.ADMIN, Role.EMPLOYEE},0,"0000000000");
        restAPITestAdapter.putNotFound(BASE_URL+'/'+user.getMail(),user);
    }

    @Test
    public void testExists() throws Exception {
        boolean bool = restAPITestAdapter.getOk(BASE_URL + "/exists/" + TEST_USER.getMail(), Boolean.class);
        assertTrue(bool);
        bool = restAPITestAdapter.getOk(BASE_URL + "/exists/" + NONEXISTING_MAIL, Boolean.class);
        assertFalse(bool);
    }
}
