package blok2.daos.db;

import blok2.daos.ILockersDao;
import blok2.helpers.Resources;
import blok2.model.reservations.LockerReservation;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DBLockersDao extends DAO implements ILockersDao {
    @Override
    public List<LockerReservation> getLockerStatusesOfLocation(String locationName) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties
                    .getString("get_lockers_statuses_of_location"));
            pstmt.setString(1, locationName);
            ResultSet rs = pstmt.executeQuery();

            List<LockerReservation> statuses = new ArrayList<>();
            while (rs.next()) {
                statuses.add(DBLockerReservationDao.createLockerReservation(rs,conn));
            }

            return statuses;
        }
    }
}
