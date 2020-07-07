package be.ugent.blok2.daos.dummies;

import be.ugent.blok2.daos.IDao;

public class ADummyDao implements IDao {
    @Override
    public void setDatabaseConnectionUrl(String url) { }

    @Override
    public void setDatabaseCredentials(String user, String password) { }

    @Override
    public void useDefaultDatabaseConnection() { }
}
