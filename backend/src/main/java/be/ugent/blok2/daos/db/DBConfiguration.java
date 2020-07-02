package be.ugent.blok2.daos.db;

import be.ugent.blok2.daos.db.ADB;
import be.ugent.blok2.model.users.Role;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBConfiguration extends ADB {

    public int getMaxPenaltyPoints(){
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(resourceBundle.getString("get_max_penalty_points"));
            resultSet.next();
            return resultSet.getInt(resourceBundle.getString("max"));
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return Integer.MAX_VALUE;
    }

    public List<Role> getRoles(){
        ArrayList<Role> roles = new ArrayList<>();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(resourceBundle.getString("get_roles"));
            while(resultSet.next()){
                Role role= Role.valueOf(resultSet.getString(resourceBundle.getString("type")));
                roles.add(role);
            }
            return roles;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return roles;
    }
}
