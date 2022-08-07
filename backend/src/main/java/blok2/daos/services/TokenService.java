package blok2.daos.services;

import blok2.daos.ITokenDao;
import blok2.daos.db.AuthConnectionProvider;
import blok2.model.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TokenService implements ITokenDao {

    private final AuthConnectionProvider connectionProvider;

    @Autowired
    public TokenService(AuthConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public List<Token> getAllTokens() {
        try (Connection connection = connectionProvider.getConnection()) {
            List<Token> tokens = new ArrayList<>();
            String query = "SELECT * FROM tokens";
            try (PreparedStatement statement = connection.prepareStatement(query);) {
                statement.execute();
                try (ResultSet resultSet = statement.getResultSet()) {
                    while (resultSet.next()) {
                        Token token = new Token();
                        token.setToken(resultSet.getString("id"));
                        token.setPurpose(resultSet.getString("purpose"));
                        token.setEmail(resultSet.getString("email"));
                        tokens.add(token);
                    }
                }
            }
            return tokens;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Token createToken(String purpose, String email) {
        if (!purpose.equals("REGISTRATION") && !purpose.equals("PASSWORD_RESET")) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Invalid purpose '" + purpose + "'. Purpose must be either 'REGISTRATION' or 'PASSWORD_RESET'"
            );
        }
        if (purpose.equals("PASSWORD_RESET") && email == null) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Email must be provided when purpose is 'PASSWORD_RESET'"
            );
        }
        try (Connection connection = connectionProvider.getConnection()) {
            // Create a new token in the database
            String query = "INSERT INTO tokens (id, purpose, email) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, UUID.randomUUID().toString());
                statement.setString(2, purpose);
                statement.setString(3, email);
                statement.executeUpdate();

                if (statement.getGeneratedKeys().next()) {
                    String id = statement.getGeneratedKeys().getString(1);
                    return new Token(id, purpose, email);
                } else {
                    throw new RuntimeException("Could not create token");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
