package blok2.daos.db;

import blok2.daos.IDao;
import org.springframework.beans.factory.annotation.Autowired;

public class DAO implements IDao {

    @Autowired
    protected ADB adb;

    public ADB getAdb() {
        return adb;
    }

}
