package blok2.daos;

import blok2.model.Token;

import java.util.List;

public interface ITokenDao {
    List<Token> getAllTokens();

    Token createToken(String purpose, String email);
}
