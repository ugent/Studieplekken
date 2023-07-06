package blok2.http.controllers;

import blok2.database.services.AuthProxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("tokens")
public class TokenController {

    private final AuthProxy authProxy;

    @Autowired
    public TokenController(AuthProxy authProxy) {
        this.authProxy = authProxy;
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> getAllTokens(HttpServletRequest request) {
        return this.authProxy.getAllTokens(request);
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> createToken(HttpServletRequest request, HttpMethod method, @RequestBody String token) throws URISyntaxException {
        return this.authProxy.createToken(request, token);
    }

}
