package blok2.security.filter;

import blok2.model.users.User;
import blok2.security.token.CustomAuthenticationToken;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private AuthenticationManager authManager;

    public CustomAuthenticationFilter(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String username = request.getHeader("AS-USER");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (username != null && auth != null) {
            Object principal = auth.getPrincipal();

            if (principal instanceof User) {
                User user = (User) principal;

                if (user.isAdmin()) {
                    Authentication token = new CustomAuthenticationToken(username);
                    Authentication authToken = authManager.authenticate(token);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
