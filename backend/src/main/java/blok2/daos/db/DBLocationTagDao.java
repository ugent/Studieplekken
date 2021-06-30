package blok2.daos.db;

import blok2.helpers.Resources;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class DBLocationTagDao extends DAO {


    public static ResultSet getTagsForLocation(int locationId, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_tags_for_location"));
        pstmt.setInt(1, locationId);
        return pstmt.executeQuery();
    }

}
