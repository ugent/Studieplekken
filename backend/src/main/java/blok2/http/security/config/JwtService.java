package blok2.http.security.config;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${custom.jwtKey}")
    private String secret;

    public Claims getClaims(String token) throws JwtException {
        return Jwts.parser()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T getValueFromClaim(String token, String key, Class<T> aClass) {
        return getClaims(token).get(key, aClass);
    }

    public boolean isValid(String token) {
        try { // Validates signature.
            Claims claims = getClaims(token);

            return isNotOutOfDate(claims);

        } catch (JwtException ex) {
            return false;
        }
    }

    private boolean isNotOutOfDate(Claims claims) {
        return claims.getExpiration().after(new Date());
    }

    public String getId(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }
}