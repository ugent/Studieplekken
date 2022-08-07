package blok2.controllers;

import blok2.daos.ITokenDao;
import blok2.model.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("tokens")
public class TokenController {

    private final ITokenDao tokenDao;

    @Autowired
    public TokenController(ITokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Token> getAllTokens() {
        return tokenDao.getAllTokens();
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('ADMIN')")
    public Token createToken(@RequestBody Token token) {
        return tokenDao.createToken(token.getPurpose(), token.getEmail());
    }

}
