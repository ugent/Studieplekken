package blok2.security.config;

import blok2.model.users.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private AuthenticationManager authManager;

    public JwtAuthenticationFilter(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwtToken = request.getHeader("X-AUTH");

        if(jwtToken != null ) {
            Authentication token = new JwtAuthenticationToken(jwtToken);
            Authentication authToken = authManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        filterChain.doFilter(request, response);
    }
}
