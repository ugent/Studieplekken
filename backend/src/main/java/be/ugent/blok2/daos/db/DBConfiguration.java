package be.ugent.blok2.daos.db;

import be.ugent.blok2.model.users.Role;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBConfiguration extends ADB {

    public List<Role> getRoles(){
        ArrayList<Role> roles = new ArrayList<>();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(databaseProperties.getString("get_roles"));
            while(resultSet.next()){
                Role role= Role.valueOf(resultSet.getString(databaseProperties.getString("type")));
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
