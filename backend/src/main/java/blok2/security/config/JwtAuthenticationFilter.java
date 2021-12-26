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
            try {
                Authentication token = new JwtAuthenticationToken(jwtToken);
                System.out.println(token.getPrincipal());
                Authentication authToken = authManager.authenticate(token);
                System.out.println(token.getPrincipal());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch(Exception e) {
                response.setStatus(401);
                response.sendError(401, "Unauthorized");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
