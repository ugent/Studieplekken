package blok2.daos.services;


import java.net.URI;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class AuthProxy {

    @Value("${custom.services.auth.baseurl}")
    private String baseUrl;
    private int tokenValiditySeconds = 10;

    @Value("${custom.services.auth.subject}")
    private String subject;
    @Value("${custom.services.auth.secret}")
    private String secret;

    private String tokenEndpoint = "/auth/tokens";

    public ResponseEntity<String> getAllTokens(HttpServletRequest request) {
        return this.mirrorRest(request, HttpMethod.GET, this.tokenEndpoint, null);
    }

    public ResponseEntity<String> createToken(HttpServletRequest request, String body) {
        return this.mirrorRest(request, HttpMethod.POST, this.tokenEndpoint, body);
    }

    private ResponseEntity<String> mirrorRest(HttpServletRequest request, HttpMethod method, String targetUrl, String body) {
        URI uri = UriComponentsBuilder.fromHttpUrl(this.baseUrl)
                                  .path(targetUrl)
                                  .query(request.getQueryString())
                                  .build(true).toUri();
    
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.set(headerName, request.getHeader(headerName));
        }

        headers.set("Authorization", "bearer " + this.generateServiceJWT());
    
        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.exchange(uri, method, httpEntity, String.class);
        } catch(HttpStatusCodeException e) {
            return ResponseEntity.status(e.getRawStatusCode())
                                 .headers(e.getResponseHeaders())
                                 .body(e.getResponseBodyAsString());
        }    
    }

    private String generateServiceJWT() {
        String jwt = Jwts.builder().setSubject(this.subject).setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + this.tokenValiditySeconds * 1000))
        .signWith(SignatureAlgorithm.HS512, this.secret.getBytes()).compact();

        return jwt;
    }
}
