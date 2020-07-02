package be.ugent.blok2;

import be.ugent.blok2.model.users.User;
import be.ugent.blok2.services.LdapService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class LdapTest {
    @Autowired
    private LdapService ldapService;

    @Test
    public void testQueryingLdap() {
        System.out.println(ldapService);
        List<User> l = ldapService.searchUserByMail("Bram.VandeWalle@UGent.be");

        for (User u : l) {
            System.out.println(u);
        }

        Assert.assertEquals(true, true);
    }
}
