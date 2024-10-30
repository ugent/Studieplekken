package blok2.security.filter;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import blok2.security.token.JwtAuthenticationToken;

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
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String jwtToken = request.getHeader("X-AUTH");

        if (jwtToken != null) {
            try {
                Authentication token = new JwtAuthenticationToken(jwtToken);
                Authentication authToken = authManager.authenticate(token);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch(AuthenticationException e) {
                response.setStatus(401);
                response.sendError(401, "Unauthorized");
                return;
            }
        }
    
        filterChain.doFilter(request, response);
    }
}
