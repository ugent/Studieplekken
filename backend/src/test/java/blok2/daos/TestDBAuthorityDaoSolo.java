package blok2.daos;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.database.dao.IAuthorityDao;
import blok2.extensions.exceptions.NoSuchDatabaseObjectException;
import blok2.model.Authority;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TestDBAuthorityDaoSolo extends BaseTest {

    @Autowired
    private IAuthorityDao authorityDao;

    private Authority testAuthority;
    private Authority testAuthority2;

    @Override
    public void populateDatabase() {
        testAuthority = TestSharedMethods.insertTestAuthority(authorityDao);
        testAuthority2 = TestSharedMethods.insertTestAuthority2(authorityDao);
    }

    @Test
    public void getAllAuthorities() {
        List<Authority> authorities = authorityDao.getAllAuthorities();
        Assert.assertEquals(2, authorities.size());
        Assert.assertTrue(authorities.contains(testAuthority));
        Assert.assertTrue(authorities.contains(testAuthority2));
    }

    @Test
    public void getAuthorityByName() {
        Authority authority = authorityDao.getAuthorityByName(testAuthority.getAuthorityName());
        Assert.assertEquals(testAuthority, authority);
    }

    @Test
    public void getAuthorityByAuthorityId() {
        Authority authority = authorityDao.getAuthorityByAuthorityId(testAuthority.getAuthorityId());
        Assert.assertEquals(testAuthority, authority);
    }

    @Test(expected = NoSuchDatabaseObjectException.class)
    public void addAndDeleteAuthority() {
        Authority authority = TestSharedMethods.insertTestAuthority("extra test authority", "testdescr", authorityDao);
        authorityDao.deleteAuthority(authority.getAuthorityId());
        authority = authorityDao.getAuthorityByAuthorityId(authority.getAuthorityId());
        Assert.assertNull(authority);
    }

    @Test
    public void updateAuthority() {
        testAuthority.setAuthorityName("different name");
        authorityDao.updateAuthority(testAuthority);
        Authority updatedAuthority = authorityDao.getAuthorityByAuthorityId(testAuthority.getAuthorityId());
        Assert.assertEquals(testAuthority, updatedAuthority);
    }

}
